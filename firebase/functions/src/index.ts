import * as admin from 'firebase-admin';
import * as faker from 'faker';
import * as functions from 'firebase-functions';

admin.initializeApp();
admin.firestore().settings({
    ignoreUndefinedProperties: true
});

const auth = admin.auth();
const firestore = admin.firestore();

function delay(ms: number) {
    return new Promise(resolve => setTimeout(resolve, ms));
}

export const createVendorBotMessage = functions.https.onCall(async (data, _context) => {
    await delay(faker.random.number({ min: 1, max: 15 }) * 1000);
    await firestore
        .collection('message_rooms').doc(data.messageRoomId)
        .collection('messages').doc()
        .create({
            sender: 'VENDOR',
            text: data.text ? data.text : faker.lorem.words(faker.random.number({ min: 1, max: 15 }))
        });
    return null;
});

export const onMessageRoomCreated = functions.firestore
    .document('message_rooms/{messageRoomId}')
    .onCreate(async (messageRoomDoc, _context) => {
        const messageRoom = messageRoomDoc.data();
        if (messageRoom === undefined) return Promise.reject('messageRoom is undefined');

        const user = (await auth.getUser(messageRoom.user.id));

        const vendor = (await firestore.collection('vendors').doc(messageRoom.vendor.id).get()).data();
        if (vendor === undefined) return Promise.reject('vendor is undefined');

        return messageRoomDoc.ref.update({
            'user.display_name': user.displayName,
            'user.display_picture_url': user.photoURL,
            'vendor.logo_url': vendor?.logo_url,
            'vendor.name': vendor?.name
        });
    });

export const onMessageSent = functions.firestore
    .document('message_rooms/{messageRoomId}/messages/{messageId}')
    .onCreate(async (messageDoc, context) => {
        return firestore.runTransaction(async transaction => {
            const message = messageDoc.data();
            if (message === undefined) return Promise.reject('message is undefined');

            return Promise.all([
                transaction.update(
                    messageDoc.ref,
                    {
                        date_and_time: admin.firestore.FieldValue.serverTimestamp()
                    }),
                transaction.update(
                    firestore.collection('message_rooms').doc(context.params.messageRoomId),
                    {
                        'last_message.date_and_time': admin.firestore.FieldValue.serverTimestamp(),
                        'last_message.id': context.params.messageId,
                        'last_message.sender': message.sender,
                        'last_message.text': message.text
                    })
            ]);
        });
    });

export const onItemAddedToCart = functions.firestore
    .document('users/{userId}/cart_items/{itemId}')
    .onCreate(async (cartItemDoc, _context) => {
        const cartItem = cartItemDoc.data();
        if (cartItem === undefined) return Promise.reject('cartItem is undefined');

        const docs = await Promise.all([
            firestore
                .collection('vendors').doc(cartItem.vendor.id)
                .collection('products').doc(cartItem.product.id)
                .get(),
            firestore.collection('vendors').doc(cartItem.vendor_id).get()
        ]);

        const product = docs[0].data();
        if (product === undefined) return Promise.reject('product is undefined');

        const vendor = docs[1].data();
        if (vendor === undefined) return Promise.reject('vendor is undefined');

        return cartItemDoc.ref.update({
            'product.name': product.name,
            'product.price': product.price,
            'quantity': 1,
            'vendor.name': vendor.name
        });
    });

export const onVendorAddedToFavorite = functions.firestore
    .document('users/{userId}/favorite_vendors/{itemId}')
    .onCreate(async (favoriteVendorDoc, _context) => {
        const favoriteVendor = favoriteVendorDoc.data();
        if (favoriteVendor === undefined) return Promise.reject('favoriteVendor is undefined');

        const vendor = (await firestore.collection('vendors').doc(favoriteVendor.vendor.id).get()).data();
        if (vendor === undefined) return Promise.reject('vendor is undefined');

        return favoriteVendorDoc.ref.update({
            'vendor.location': vendor.location,
            'vendor.logo_url': vendor.logo_url,
            'vendor.name': vendor.name,
            'vendor.rating': vendor.rating,
            'vendor.review_number': vendor.review_number
        });
    });

export const onTransactionCreated = functions.firestore
    .document('users/{userId}/transactions/{transactionId}')
    .onCreate(async (transactionDoc, context) => {
        const promises = [];

        let total = 0;

        const cartItemQuery = await firestore
            .collection('users').doc(context.params.userId)
            .collection('cart_items')
            .get();

        for (const cartItemDoc of cartItemQuery.docs) {
            const cartItem = cartItemDoc.data();
            const subtotal = cartItem.quantity * cartItem.product.price;
            total += subtotal;
            promises.push(transactionDoc.ref.collection('transaction_items').doc().create({
                message: cartItem.message,
                product: cartItem.product,
                quantity: cartItem.quantity,
                subtotal: subtotal
            }));
            promises.push(cartItemDoc.ref.delete());
        }

        promises.push(transactionDoc.ref.update({
            date_and_time: admin.firestore.FieldValue.serverTimestamp(),
            status: 'AWAITING_PAYMENT',
            total: total,
            vendor: cartItemQuery.docs[0].data().vendor
        }));

        return Promise.all(promises);
    });

export const onVendorCreated = functions.firestore
    .document('vendors/{vendorId}')
    .onCreate(async (vendorDoc, context) => {
        const vendor = vendorDoc.data();
        if (vendor === undefined) return Promise.reject('vendor is undefined');

        return firestore.collection('vendor_search_snippets').doc(context.params.vendorId).create({
            location: vendor.location,
            logo_url: vendor.logo_url,
            name: vendor.name,
            services: vendor.services
        });
    });

export const onVendorProductCreated = functions.firestore
    .document('vendors/{vendorId}/products/{productId}')
    .onCreate(async (productDoc, context) => {
        return firestore.runTransaction(async transaction => {
            const product = productDoc.data();
            if (product === undefined) return Promise.reject('product is undefined');

            const vendorDocRef = firestore.collection('vendors').doc(context.params.vendorId);
            const vendor = (await transaction.get(vendorDocRef)).data();
            if (vendor === undefined) return Promise.reject('vendor is undefined');

            const lastAverageProductPrice = vendor.average_product_price ? vendor.average_product_price : 0;
            const lastProductNumber = vendor.product_number ? vendor.product_number : 0;
            const newAverageProductPrice = ((lastAverageProductPrice * lastProductNumber) + product.price) / (lastProductNumber + 1);

            let priceRange;
            if (newAverageProductPrice < 15_000_000) priceRange = 'LOW';
            else if (newAverageProductPrice < 30_000_000) priceRange = 'MEDIUM';
            else priceRange = 'HIGH';

            return transaction.update(
                vendorDocRef,
                {
                    average_product_price: newAverageProductPrice,
                    price_range: priceRange,
                    product_number: lastProductNumber + 1
                }
            );
        });
    });

export const onVendorReviewed = functions.firestore
    .document('vendors/{vendorId}/reviews/{reviewId}')
    .onCreate(async (reviewDoc, context) => {
        return firestore.runTransaction(async transaction => {
            const review = reviewDoc.data();
            if (review === undefined) return Promise.reject('review is undefined');

            const user = await auth.getUser(review.user.id);
            if (user === undefined) return Promise.reject('user is undefined');

            const vendorDocRef = firestore.collection('vendors').doc(context.params.vendorId);
            const vendor = (await transaction.get(vendorDocRef)).data();
            if (vendor === undefined) return Promise.reject('vendor is undefined');

            const lastRating = vendor.rating ? vendor.rating : 0;
            const lastReviewNumber = vendor.review_number ? vendor.review_number : 0;
            const newRating = ((lastRating * lastReviewNumber) + review.rating) / (lastReviewNumber + 1);

            const latestReviewsPosition = vendor.latest_reviews ? (vendor.latest_reviews.position ? vendor.latest_reviews.position : 0) : 0;

            return Promise.all([
                transaction.update(
                    reviewDoc.ref,
                    {
                        'date_and_time': admin.firestore.FieldValue.serverTimestamp(),
                        'user.display_name': user.displayName,
                        'user.display_picture_url': user.photoURL
                    }
                ),
                transaction.update(
                    vendorDocRef,
                    {
                        [`latest_reviews.${latestReviewsPosition}`]: {
                            comment: review.comment,
                            date_and_time: admin.firestore.FieldValue.serverTimestamp(),
                            id: context.params.reviewId,
                            rating: review.rating,
                            user: {
                                display_name: user.displayName,
                                id: review.user.id,
                                display_picture_url: user.photoURL
                            }
                        },
                        'latest_reviews.position': latestReviewsPosition + 1 === 5 ? 0 : latestReviewsPosition + 1,
                        'rating': newRating,
                        'review_number': lastReviewNumber + 1
                    }
                )
            ]);
        });
    });

export const onVendorUpdated = functions.firestore
    .document('vendors/{vendorId}')
    .onUpdate(async (change, context) => {
        return firestore.runTransaction(async transaction => {
            const vendor = change.after.data();
            if (vendor === undefined) return Promise.reject('vendor is undefined');

            if (change.before.data() === vendor) return Promise.resolve();

            const promises = [];

            //message_rooms
            if (vendor.logo_url !== undefined || vendor.name !== undefined) {
                console.info('updating message_rooms vendor');
                console.info(`vendor.logo_url: ${vendor.logo_url}`);
                console.info(`vendor.name: ${vendor.name}`);
                (await firestore.collection('message_rooms').where('vendor.id', '==', context.params.vendorId).get())
                    .docs.forEach(doc => {
                        promises.push(transaction.update(
                            doc.ref,
                            {
                                'vendor.logo_url': vendor.logo_url,
                                'vendor.name': vendor.name
                            })
                        );
                    });
            }

            //cart_items
            if (vendor.name !== undefined) {
                console.info('updating cart_items vendor');
                console.info(`vendor.name: ${vendor.name}`);
                (await firestore.collectionGroup('cart_items').where('vendor.id', '==', context.params.vendorId).get())
                    .docs.forEach(doc => {
                        promises.push(transaction.update(
                            doc.ref,
                            {
                                'vendor.name': vendor.name
                            })
                        );
                    });
            }

            //favorite_vendors
            if (
                vendor.location !== undefined ||
                vendor.logo_url !== undefined ||
                vendor.name !== undefined ||
                vendor.rating !== undefined ||
                vendor.review_number !== undefined
            ) {
                console.info('updating favorite_vendors vendor');
                console.info(`vendor.location: ${vendor.location}`);
                console.info(`vendor.logo_url: ${vendor.logo_url}`);
                console.info(`vendor.name: ${vendor.name}`);
                console.info(`vendor.rating: ${vendor.rating}`);
                console.info(`vendor.review_number: ${vendor.review_number}`);
                (await firestore.collectionGroup('favorite_vendors').where('vendor.id', '==', context.params.vendorId).get())
                    .docs.forEach(doc => {
                        promises.push(transaction.update(
                            doc.ref,
                            {
                                'vendor.location': vendor.location,
                                'vendor.logo_url': vendor.logo_url,
                                'vendor.name': vendor.name,
                                'vendor.rating': vendor.rating,
                                'vendor.review_number': vendor.review_number
                            })
                        );
                    });
            }

            //transactions
            if (vendor.name !== undefined) {
                console.info('updating transactions vendor');
                console.info(`vendor.name: ${vendor.name}`);
                (await firestore.collectionGroup('transactions').where('vendor.id', '==', context.params.vendorId).get())
                    .docs.forEach(doc => {
                        promises.push(
                            transaction.update(doc.ref, {
                                'vendor.name': vendor.name
                            })
                        );
                    });
            }

            //vendor_search_snippets
            if (
                vendor.location !== undefined ||
                vendor.logo_url !== undefined ||
                vendor.name !== undefined ||
                vendor.rating !== undefined ||
                vendor.review_number !== undefined
            ) {
                console.info('updating vendor_search_snippets vendor');
                console.info(`vendor.location: ${vendor.location}`);
                console.info(`vendor.logo_url: ${vendor.logo_url}`);
                console.info(`vendor.name: ${vendor.name}`);
                console.info(`vendor.rating: ${vendor.rating}`);
                console.info(`vendor.review_number: ${vendor.review_number}`);
                promises.push(transaction.update(
                    firestore.collection('vendor_search_snippets').doc(context.params.vendorId),
                    {
                        'location': vendor.location,
                        'logo_url': vendor.logo_url,
                        'name': vendor.name,
                        'rating': vendor.rating,
                        'review_number': vendor.review_number,
                        'services': vendor.services
                    }
                ));
            }

            return Promise.all(promises);
        });
    });

export const updateUserData = functions.https.onCall(async (data, _context) => {
    const promises: Promise<any>[] = [];

    //message_rooms
    if (data.user.display_name !== undefined || data.user.display_picture_url) {
        console.info('updating message_rooms user');
        console.info(`user.display_name: ${data.user.display_name}`);
        console.info(`user.display_picture_url: ${data.user.display_picture_url}`);
        (await firestore.collection('message_rooms').where('user.id', '==', data.user.id).get())
            .docs.forEach(doc => {
                promises.push(doc.ref.update({
                    'user.display_name': data.user.display_name,
                    'user.display_picture_url': data.user.display_picture_url
                }));
            });
    }

    //vendors

    //reviews
    if (data.user.display_name !== undefined || data.user.display_picture_url) {
        console.info('updating reviews user');
        console.info(`user.display_name: ${data.user.display_name}`);
        console.info(`user.display_picture_url: ${data.user.display_picture_url}`);
        (await firestore.collectionGroup('reviews').where('user.id', '==', data.user.id).get())
            .docs.forEach(doc => {
                promises.push(doc.ref.update({
                    'user.display_name': data.user.display_name,
                    'user.display_picture_url': data.user.display_picture_url
                }));
            });
    }

    await Promise.all(promises);
    return null;
});

export const onProductUpdated = functions.firestore
    .document('vendors/{vendorId}/products/{productId}')
    .onUpdate(async (change, context) => {
        const product = change.after.data();
        if (product === undefined) return Promise.reject('product is undefined');

        if (change.before.data() === product) return Promise.resolve();

        const promises: Promise<any>[] = [];

        //cart_items
        if (product.name !== undefined || product.price !== undefined) {
            console.info('updating cart_items product');
            console.info(`product.name: ${product.name}`);
            console.info(`product.price: ${product.price}`);
            (await firestore.collectionGroup('cart_items').where('product.id', '==', context.params.productId).get())
                .docs.forEach(doc => {
                    promises.push(doc.ref.update(
                        {
                            'product.name': product.name,
                            'product.price': product.price
                        })
                    );
                });
        }

        //vendors

        return Promise.all(promises);
    });

//onReviewUpdated

import * as admin from 'firebase-admin';
import * as faker from 'faker';
import * as seedData from './seed-data';
const prompts = require('prompts');

admin.initializeApp({
    credential: admin.credential.cert(require('./../../../secrets/firebase-admin-sdk-key.json')),
    databaseURL: 'https://halalin-c8f9b.firebaseio.com'
});

const auth = admin.auth();
const firestore = admin.firestore();

const userList: User[] = [];
const serviceList: Service[] = [];

interface Vendor {
    contacts?: Contact;
    description?: string;
    location?: string;
    logoUrl?: string;
    name?: string;
    services?: Service[];
}

interface Contact {
    facebook?: string;
    instagram?: string;
    phone?: string;
    website?: string;
    whatsapp?: string
}

interface Service {
    iconUrl?: string;
    id?: string;
    imageUrl?: string;
    name?: string;
}

interface Product {
    description?: string,
    imageUrl?: string,
    name?: string,
    price?: number
}

interface Review {
    comment?: string,
    rating?: number,
    user?: User
}

interface User {
    displayName?: string,
    displayPictureUrl?: string,
    emailAddress?: string,
    id?: string
}

function delay(ms: number) {
    return new Promise(resolve => setTimeout(resolve, ms));
}

function generateVendor(): Vendor {
    const name = faker.company.companyName(0);
    return {
        contacts: generateContacts(name),
        description: faker.lorem.paragraph(faker.random.number({ min: 10, max: 20 })),
        location: faker.fake('{{address.cityPrefix}} {{address.citySuffix}}, {{address.state}}'),
        logoUrl: `https://logoipsum.com/logo/logo-${faker.random.number({ min: 1, max: 12 })}.svg`,
        name: name,
        services: getRandomServices()
    }
}

function generateContacts(vendorName: string): Contact {
    const phoneNumber = generatePhoneNumber();
    const username = vendorName.replace(/\W/g, '').toLowerCase();
    const contact: Contact = { phone: phoneNumber };
    if (faker.random.boolean()) contact.facebook = username;
    if (faker.random.boolean()) contact.instagram = username;
    if (faker.random.boolean()) contact.website = `https://${username}.com/`;
    if (faker.random.boolean()) contact.whatsapp = phoneNumber;
    return contact;
}

function generatePhoneNumber(): string {
    let phoneNumber = '+62';
    phoneNumber += faker.random.number({ min: 2, max: 9 });
    for (let i = 0; i < 2 + 6; i++) {
        phoneNumber += faker.random.number({ min: 1, max: 9 });
    }
    return phoneNumber;
}

function getRandomServices(): Service[] {
    const services: Service[] = [];
    for (let i = 0; i < faker.random.number({ min: 1, max: 5 }); i++) {
        let service: Service, index: number;
        do {
            service = serviceList[faker.random.number({ min: 0, max: serviceList.length - 1 })];
            index = services.findIndex(value => value.id === service.id);
        } while (index !== -1)
        services.push(service);
    }
    return services;
}

function generateProduct(): Product {
    return {
        description: faker.lorem.sentences(faker.random.number({ min: 5, max: 10 })),
        imageUrl: `https://picsum.photos/seed/${faker.lorem.word()}/500`,
        name: generateProductName(),
        price: (faker.random.number({ min: 1, max: 50 }) * 1_000_000) + (faker.random.number({ min: 0, max: 9 }) * 100_000)
    }
}

function generateProductName(): string {
    let name = '';
    for (let i = 0; i < faker.random.number({ min: 3, max: 5 }); i++) {
        const word = faker.lorem.word();
        name += ` ${word[0].toUpperCase() + word.substring(1)}`;
    }
    return name.substring(1, name.length);
}

function generateReview(): Review {
    return {
        comment: faker.lorem.sentences(faker.random.number({ min: 1, max: 5 })),
        rating: faker.random.number({ min: 3, max: 5 })
    }
}

function generateUser(): User {
    const name = faker.name.findName();
    return {
        displayName: name,
        displayPictureUrl: `https://i.pravatar.cc/250?img=${faker.random.number({ min: 1, max: 70 })}`,
        emailAddress: `${name.replace(/\W/g, '')}@${faker.lorem.word()}.com`.toLowerCase()
    }
}

async function seedServices() {
    const seeded = (await firestore.collection('services').get()).empty;
    if (!seeded) {
        console.log('services already seeded, cancelling operation...');
        return;
    }

    await Promise.all(
        seedData.services.map(service =>
            firestore.collection('services').doc().create({
                icon_url: service.iconUrl,
                image_url: service.imageUrl,
                name: service.name
            })
        )
    );
}

async function deleteAllAnonymousUser() {
    const userIds = (await auth.listUsers()).users
        .filter(user => user.email === undefined)
        .map(user => user.uid);
    console.log(`deleting ${userIds.length} anonymous users...`);
    const result = await auth.deleteUsers(userIds);
    console.log(`delete done. success: ${result.successCount}, failure: ${result.failureCount}`);
}

async function populateUser() {
    const promptResponse = await prompts({
        type: 'number',
        name: 'number',
        message: 'how many?'
    });

    const usersEmailAddresses = (await auth.listUsers()).users
        .filter(user => user.email)
        .map(user => user.email);
    console.log(`${usersEmailAddresses.length} users registered`);

    for (let i = 0, max = promptResponse.number; i < max; i++) {
        console.log(`populating ${i + 1}/${max}...`);

        let userIsDuplicate: boolean, user: User;
        do {
            user = generateUser();
            userIsDuplicate = usersEmailAddresses.findIndex(
                emailAddress => emailAddress === user.emailAddress
            ) !== -1;
        } while (userIsDuplicate);
        console.log(user);

        console.log('registering user...');
        await auth.createUser({
            displayName: user.displayName,
            email: user.emailAddress,
            emailVerified: true,
            password: 'password',
            photoURL: user.displayPictureUrl
        });
        usersEmailAddresses.push(user.emailAddress);
        console.log('user registered.');
    }
    console.log('populating done.');
}

async function populateVendor() {
    const promptResponse = await prompts({
        type: 'number',
        name: 'number',
        message: 'how many?'
    });

    const vendorNames = (await firestore
        .collection('vendors')
        .select('name')
        .get()
    )
        .docs.map(doc => doc.data().name);
    console.log(`${vendorNames.length} vendors registered`);

    const userIds = (await auth.listUsers()).users
        .filter(user => user.email)
        .map(user => user.uid);

    for (let i = 0, max = promptResponse.number; i < max; i++) {
        console.log(`populating vendor ${i + 1}/${max}...`);

        let vendorRegistered: boolean, generatedVendor: Vendor;
        do {
            generatedVendor = generateVendor();
            vendorRegistered = vendorNames.indexOf(generatedVendor.name) !== -1;
        } while (vendorRegistered);
        console.log(generatedVendor);

        console.log('registering vendor...');
        const vendorDoc = firestore.collection('vendors').doc();
        await vendorDoc.create({
            contacts: generatedVendor.contacts,
            description: generatedVendor.description,
            location: generatedVendor.location,
            logo_url: generatedVendor.logoUrl,
            name: generatedVendor.name,
            services: generatedVendor.services.map(service => service.id)
        });
        vendorNames.push(generatedVendor.name);
        console.log('vendor registered.');

        await populateVendorProduct(vendorDoc.id);
        await populateVendorReview(vendorDoc.id, userIds);
    }
    console.log('populating vendor done.');
}

async function populateVendorProduct(vendorId: string) {
    const productNames = [];
    for (let i = 0, max = faker.random.number({ min: 3, max: 7 }); i < max; i++) {
        console.log(`populating vendor (${vendorId}) product ${i + 1}/${max}...`);

        let productNameRegistered: boolean, generatedProduct: Product;
        do {
            generatedProduct = generateProduct();
            productNameRegistered = productNames.findIndex(name => name === generatedProduct.name) !== -1;
        } while (productNameRegistered);
        console.log(generatedProduct);

        console.log('uploading product...');
        await firestore
            .collection('vendors').doc(vendorId)
            .collection('products').doc()
            .create({
                description: generatedProduct.description,
                image_url: generatedProduct.imageUrl,
                name: generatedProduct.name,
                price: generatedProduct.price
            });
        productNames.push(generatedProduct.name);
        console.log('product uploaded.');

        await delay(100);
    }
    console.log(`populating vendor (${vendorId}) product done.`);
}

async function populateVendorReview(vendorId: string, registeredUserIds: string[]) {
    const reviewUserIds = [];
    for (let i = 0, max = faker.random.number({ min: 0, max: 10 }); i < max; i++) {
        console.log(`populating vendor (${vendorId}) review ${i + 1}/${max}...`);

        let userReviewed: boolean, userId: string;
        do {
            userId = registeredUserIds[faker.random.number({ min: 0, max: registeredUserIds.length - 1 })];
            userReviewed = reviewUserIds.indexOf(userId) !== -1;
        } while (userReviewed);
        const generatedReview: Review = generateReview();
        generatedReview.user = { id: userId }
        console.log(generatedReview);

        console.log('uploading review...');
        await firestore
            .collection('vendors').doc(vendorId)
            .collection('reviews').doc()
            .create({
                comment: generatedReview.comment,
                rating: generatedReview.rating,
                user: generatedReview.user
            });
        reviewUserIds.push(userId);
        console.log('review uploaded.');

        await delay(100);
    }
    console.log(`populating vendor (${vendorId}) review done.`);
}

async function createUsersDoc() {
    const promises = [];
    (await auth.listUsers()).users.forEach(user => {
        promises.push(firestore.collection('users').doc(user.uid).set({
            created: true
        }));
    });
    await Promise.all(promises);
}

const menu = [
    {
        title: 'seed services',
        function: seedServices
    },
    {
        title: 'delete all anonymous user',
        function: deleteAllAnonymousUser
    },
    {
        title: 'populate user',
        function: populateUser
    },
    {
        title: 'create users doc',
        function: createUsersDoc
    },
    {
        title: 'populate vendor',
        function: populateVendor
    }
];

async function main() {
    (await auth.listUsers()).users.forEach(user => {
        userList.push({
            displayName: user.displayName,
            displayPictureUrl: user.photoURL,
            emailAddress: user.email,
            id: user.uid
        });
    });

    (await firestore.collection('services').get()).docs.forEach(doc => {
        const service = doc.data();
        serviceList.push({
            iconUrl: service.icon_url,
            id: doc.id,
            imageUrl: service.image_url,
            name: service.name
        });
    });

    let exit = false;
    while (!exit) {
        const selectMenuPromptResponse = await prompts({
            type: 'select',
            name: 'value',
            message: 'select menu',
            choices: menu.map((value, i) => {
                return {
                    title: value.title,
                    value: i
                }
            })
        });
        await menu[selectMenuPromptResponse.value].function();

        const exitPromptResponse = await prompts({
            type: 'confirm',
            name: 'value',
            message: 'exit?'
        });
        exit = exitPromptResponse.value;
    }
    console.log('press Ctrl + C to stop');
}

main();

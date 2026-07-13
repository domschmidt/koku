import { APIRequestContext, APIResponse, request } from '@playwright/test';
import { readE2eCredentials } from './credentials';

interface AccessTokenResponse {
  readonly access_token: string;
}

interface CreatedEntity {
  readonly id: number;
}

export default async function globalSetup(): Promise<void> {
  if (process.env['KOKU_E2E_SEED'] !== 'true') {
    return;
  }

  const baseURL = requiredEnvironmentVariable('KOKU_E2E_BASE_URL');
  const keycloakURL = requiredEnvironmentVariable('KOKU_E2E_KEYCLOAK_URL');
  const credentials = readE2eCredentials();
  const accessToken = await requestAccessToken(keycloakURL, credentials.username, credentials.password);
  const api = await request.newContext({
    baseURL,
    extraHTTPHeaders: {
      Authorization: `Bearer ${accessToken}`,
    },
  });

  try {
    await seedBaseline(api);
  } finally {
    await api.dispose();
  }
}

async function requestAccessToken(keycloakURL: string, username: string, password: string): Promise<string> {
  const keycloak = await request.newContext({ baseURL: keycloakURL });

  try {
    const response = await keycloak.post('/realms/koku/protocol/openid-connect/token', {
      form: {
        client_id: 'koku',
        grant_type: 'password',
        password,
        scope: 'openid profile',
        username,
      },
    });
    await expectSuccessfulResponse(response, 'request an E2E access token');
    const token = (await response.json()) as AccessTokenResponse;
    if (!token.access_token) {
      throw new Error('Keycloak returned no access token for the E2E user.');
    }
    return token.access_token;
  } finally {
    await keycloak.dispose();
  }
}

async function seedBaseline(api: APIRequestContext): Promise<void> {
  await postWithoutResponse(api, '/services/users/users/@self/sync', {});

  const manufacturer = await post<CreatedEntity>(api, '/services/products/productmanufacturers', {
    deleted: false,
    name: 'E2E Hersteller Bestand',
  });

  await post(api, '/services/products/products', {
    deleted: false,
    manufacturerId: manufacturer.id,
    name: 'E2E Produkt Bestand',
    price: 19.95,
  });
  await post(api, '/services/activities/activities', {
    approximatelyDuration: '00:30:00',
    deleted: false,
    name: 'E2E Tätigkeit Bestand',
    price: 25,
  });
  await post(api, '/services/activities/activitysteps', {
    deleted: false,
    name: 'E2E Tätigkeitsschritt Bestand',
  });
  await post(api, '/services/promotions/promotions', {
    activityAbsoluteItemSavings: 0,
    activityAbsoluteSavings: 0,
    activityRelativeItemSavings: 0,
    activityRelativeSavings: 0,
    deleted: false,
    longSummary: 'Automatisch erzeugte E2E-Basispromotion',
    name: 'E2E Promotion Bestand',
    productAbsoluteItemSavings: 0,
    productAbsoluteSavings: 0,
    productRelativeItemSavings: 0,
    productRelativeSavings: 0,
    shortSummary: 'E2E Basispromotion',
  });
  await post(api, '/services/customers/customers', {
    deleted: false,
    firstName: 'E2E',
    lastName: 'Bestand',
    onFirstnameBasis: false,
  });
}

async function post<T>(api: APIRequestContext, url: string, data: object): Promise<T> {
  const response = await api.post(url, { data });
  await expectSuccessfulResponse(response, `seed ${url}`);
  return (await response.json()) as T;
}

async function postWithoutResponse(api: APIRequestContext, url: string, data: object): Promise<void> {
  const response = await api.post(url, { data });
  await expectSuccessfulResponse(response, `seed ${url}`);
}

async function expectSuccessfulResponse(response: APIResponse, operation: string): Promise<void> {
  if (!response.ok()) {
    throw new Error(`Unable to ${operation}: HTTP ${response.status()} ${await response.text()}`);
  }
}

function requiredEnvironmentVariable(name: string): string {
  const value = process.env[name];
  if (!value) {
    throw new Error(`Set ${name} to seed the E2E environment.`);
  }
  return value;
}

export interface E2eCredentials {
  readonly password: string;
  readonly username: string;
}

export function hasE2eCredentials(): boolean {
  return Boolean(process.env['KOKU_E2E_USERNAME'] && process.env['KOKU_E2E_PASSWORD']);
}

export function readE2eCredentials(): E2eCredentials {
  const username = process.env['KOKU_E2E_USERNAME'];
  const password = process.env['KOKU_E2E_PASSWORD'];

  if (!username || !password) {
    throw new Error('Set KOKU_E2E_USERNAME and KOKU_E2E_PASSWORD to run authenticated E2E tests.');
  }

  return { username, password };
}

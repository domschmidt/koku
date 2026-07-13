import { ApplicationInitStatus } from '@angular/core';
import { TestBed } from '@angular/core/testing';
import { vi } from 'vitest';
import { appConfig } from './app.config';
import { AuthService } from './auth/auth.service';

describe('appConfig', () => {
  it('runs the authentication application initializer', async () => {
    const initialize = vi.fn(() => Promise.resolve());
    TestBed.configureTestingModule({
      providers: [...appConfig.providers, { provide: AuthService, useValue: { initialize } }],
    });

    await TestBed.inject(ApplicationInitStatus).donePromise;
    expect(initialize).toHaveBeenCalled();
  });
});

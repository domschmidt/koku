import { HttpClient } from '@angular/common/http';
import { TestBed } from '@angular/core/testing';
import { firstValueFrom, of } from 'rxjs';
import { describe, expect, it, vi } from 'vitest';
import { MyUserDetailsService } from './my-user-details.service';

describe('MyUserDetailsService', () => {
  it('shares current-user requests and supports an explicit refresh', async () => {
    let userId = 0;
    const http = { get: vi.fn(() => of({ id: ++userId })) };
    TestBed.configureTestingModule({
      providers: [MyUserDetailsService, { provide: HttpClient, useValue: http }],
    });
    const service = TestBed.inject(MyUserDetailsService);
    expect(await firstValueFrom(service.getCurrentUserDetailsCached())).toEqual({ id: 1 });
    expect(await firstValueFrom(service.getCurrentUserDetailsCached())).toEqual({ id: 1 });
    expect(http.get).toHaveBeenCalledOnce();
    expect(await firstValueFrom(service.getCurrentUserDetailsCached(true))).toEqual({ id: 2 });
    expect(http.get).toHaveBeenCalledTimes(2);
  });
});

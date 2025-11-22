import {inject, Injectable} from '@angular/core';
import {defer, Observable, shareReplay} from "rxjs";
import {HttpClient} from "@angular/common/http";

@Injectable({
  providedIn: 'root'
})
export class MyUserDetailsService {

  private cache?: Observable<KokuDto.KokuUserDto>;
  private readonly httpClient = inject(HttpClient);

  getCurrentUserDetailsCached(forceRefresh = false): Observable<KokuDto.KokuUserDto> {
    if (forceRefresh || !this.cache) {
      this.cache = defer(() =>
        this.httpClient.get<KokuDto.KokuUserDto>('/services/users/users/@self')
      ).pipe(
        shareReplay({ bufferSize: 1, refCount: false })
      );
    }

    return this.cache;
  }

}

import {Injectable} from '@angular/core';
import {HttpClient} from "@angular/common/http";
import {Observable} from "rxjs";
import {finalize} from "rxjs/operators";

@Injectable({
  providedIn: 'root'
})
export class AuthService {

  constructor(private readonly httpClient: HttpClient) {
  }

  createSession(username: string, password: string): Observable<undefined> {
    return new Observable<undefined>((observer) => {
      const apiEndpoint = '/backend/auth/login';
      const requestBody: KokuDto.LoginDto = {
        username,
        password
      };
      this.httpClient.post<KokuDto.LoginAttemptResponseDto>(
        apiEndpoint,
        requestBody
      ).subscribe(() => {
        observer.next();
        observer.complete();
      }, (error) => {
        observer.error(error);
      });
    });
  }

  refreshToken() {
    return new Observable<undefined>((observer) => {
      const apiEndpoint = '/backend/auth/refresh';
      this.httpClient.post<KokuDto.LoginAttemptResponseDto>(apiEndpoint, {}).subscribe(() => {
        observer.next();
        observer.complete();
      }, (error) => {
        observer.error(error);
      });
    });
  }

  destroySession() {
    return this.httpClient.post('/backend/auth/logout', {}).pipe(finalize(() => {
      window.localStorage.clear();
    }));
  }

}

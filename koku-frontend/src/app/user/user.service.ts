import {Injectable} from '@angular/core';
import {BehaviorSubject, Observable} from "rxjs";
import {HttpClient, HttpParams} from "@angular/common/http";

@Injectable({
  providedIn: 'root'
})
export class UserService {
  private _users: BehaviorSubject<KokuDto.KokuUserDetailsDto[]> = new BehaviorSubject(new Array<KokuDto.KokuUserDetailsDto>());

  public readonly users: Observable<KokuDto.KokuUserDetailsDto[]> = this._users.asObservable();

  constructor(public httpClient: HttpClient) {
  }

  public getUsers(searchValue?: string) {
    this.loadUsers(searchValue).subscribe((result) => {
      this._users.next(result)
    });
    return this.users;
  }

  getUser(userId: number) {
    return this.httpClient.get<KokuDto.KokuUserDetailsDto>(`/backend/users/${userId}`);
  }

  createUser(user: KokuDto.KokuUserDetailsDto) {
    return new Observable<KokuDto.KokuUserDetailsDto>((observer) => {
      return this.httpClient.post<KokuDto.KokuUserDetailsDto>(`/backend/users`, user).subscribe((result: KokuDto.KokuUserDetailsDto) => {
        this.loadUsers().subscribe((newResult) => {
          this._users.next(newResult);
          observer.next(result);
          observer.complete();
        }, (error) => {
          observer.error(error);
        })
      }, (error) => {
        observer.error(error);
      });
    });
  }

  updateUser(user: KokuDto.KokuUserDetailsDto) {
    return new Observable((observer) => {
      return this.httpClient.put(`/backend/users/${user.id}`, user).subscribe(() => {
        this.loadUsers().subscribe((newResult) => {
          this._users.next(newResult);
          observer.next();
          observer.complete();
        }, (error) => {
          observer.error(error);
        })
      }, (error) => {
        observer.error(error);
      });
    });
  }

  deleteUser(user: KokuDto.KokuUserDetailsDto) {
    return new Observable((observer) => {
      return this.httpClient.delete(`/backend/users/${user.id}`).subscribe(() => {
        this.loadUsers().subscribe((newResult) => {
          this._users.next(newResult);
          observer.next();
          observer.complete();
        }, (error) => {
          observer.error(error);
        })
      }, (error) => {
        observer.error(error);
      });
    });
  }

  private loadUsers(searchValue?: string) {
    const params = new HttpParams({
      fromObject: {
        search: searchValue || ''
      }
    });
    return this.httpClient.get<KokuDto.KokuUserDetailsDto[]>('/backend/users', {params});
  }

}

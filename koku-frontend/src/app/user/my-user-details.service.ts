import {Injectable} from '@angular/core';
import {BehaviorSubject, Observable} from "rxjs";
import {HttpClient} from "@angular/common/http";
import {AuthService} from "../auth.service";

@Injectable({
  providedIn: 'root'
})
export class MyUserDetailsService {
  private _mydetails: BehaviorSubject<KokuDto.KokuUserDetailsDto> = new BehaviorSubject({});

  public readonly details: Observable<KokuDto.KokuUserDetailsDto> = this._mydetails.asObservable();

  constructor(
    public httpClient: HttpClient,
    private readonly authService: AuthService
  ) {
  }

  public getDetails() {
    this.loadDetails().subscribe((result) => {
      this._mydetails.next(result);
    });
    return this.details;
  }

  private loadDetails() {
    return this.httpClient.get<KokuDto.KokuUserDetailsDto>('/backend/users/@self');
  }

  updateDetails(userDetails: KokuDto.KokuUserDetailsDto) {
    return new Observable((observer) => {
      return this.httpClient.put('/backend/users/@self', userDetails).subscribe(() => {
        this.authService.refreshToken().subscribe(() => {
          this.loadDetails().subscribe((newResult) => {
            this._mydetails.next(newResult);
            observer.next();
            observer.complete();
          }, (error) => {
            observer.error(error);
          });
        });
      }, (error) => {
        observer.error(error);
      });
    });
  }

}

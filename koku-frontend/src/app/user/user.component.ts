import {Component} from '@angular/core';
import {Observable, Subject} from "rxjs";
import {MatDialog} from "@angular/material/dialog";
import {debounceTime, distinctUntilChanged} from "rxjs/operators";
import {UserDetailsComponent, UserDetailsComponentData} from "./user-details/user-details.component";
import {UserService} from "./user.service";

@Component({
  selector: 'user',
  templateUrl: './user.component.html',
  styleUrls: ['./user.component.scss']
})
export class UserComponent {

  users$: Observable<KokuDto.KokuUserDetailsDto[]>;
  searchFieldChangeSubject: Subject<string> = new Subject<string>();
  searchFieldModel: string = "";

  constructor(public dialog: MatDialog,
              public userService: UserService) {
    this.users$ = this.userService.getUsers();

    this.searchFieldChangeSubject.asObservable().pipe(
      debounceTime(150), // wait 300ms after the last event before emitting last event
      distinctUntilChanged() // only emit if value is different from previous value
    ).subscribe(debouncedValue => this.userService.getUsers(debouncedValue));
  }

  openUserDetails(user: KokuDto.KokuUserDetailsDto) {
    const dialogData: UserDetailsComponentData = {
      userId: user.id || 0
    };
    this.dialog.open(UserDetailsComponent, {
      data: dialogData,
      closeOnNavigation: false,
      position: {
        top: '20px'
      }
    });
  }

  trackByFn(index: number, item: KokuDto.KokuUserDetailsDto) {
    return item.id;
  }

  addNewUser() {
    const dialogData: UserDetailsComponentData = {};
    this.dialog.open(UserDetailsComponent, {
      data: dialogData,
      closeOnNavigation: false,
      position: {
        top: '20px'
      }
    });
  }

  clearSearchField() {
    this.searchFieldModel = "";
    this.searchFieldChangeSubject.next("");
  }

}

import {Component} from '@angular/core';
import {MatDialog, MatDialogRef} from "@angular/material/dialog";
import {Observable, Subject} from "rxjs";
import {UserService} from "../user.service";
import {debounceTime, distinctUntilChanged} from "rxjs/operators";

export interface UserSelectionComponentData {
}

export interface UserSelectionComponentResponseData {
  user: KokuDto.KokuUserDetailsDto;
}

@Component({
  selector: 'user-selection',
  templateUrl: './user-selection.component.html',
  styleUrls: ['./user-selection.component.scss']
})
export class UserSelectionComponent {

  users$: Observable<KokuDto.KokuUserDetailsDto[]>;
  searchFieldChangeSubject: Subject<string> = new Subject<string>();
  searchFieldModel: string = "";

  constructor(public dialog: MatDialog,
              public dialogRef: MatDialogRef<UserSelectionComponent>,
              public userService: UserService) {
    this.users$ = this.userService.getUsers();

    this.searchFieldChangeSubject.asObservable().pipe(
      debounceTime(150), // wait 300ms after the last event before emitting last event
      distinctUntilChanged() // only emit if value is different from previous value
    ).subscribe(debouncedValue => this.userService.getUsers(debouncedValue));
  }

  selectUser(user: KokuDto.KokuUserDetailsDto) {
    const dialogData: UserSelectionComponentResponseData = {
      user
    };
    this.dialogRef.close(dialogData);
  }

  trackByFn(index: number, item: KokuDto.KokuUserDetailsDto) {
    return item.id;
  }

  clearSearchField() {
    this.searchFieldModel = "";
    this.searchFieldChangeSubject.next("");
  }

}

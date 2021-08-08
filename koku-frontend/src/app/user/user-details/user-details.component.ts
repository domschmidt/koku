import {Component, Inject} from '@angular/core';
import {MAT_DIALOG_DATA, MatDialog, MatDialogRef} from "@angular/material/dialog";
import {NgForm} from "@angular/forms";
import {UserService} from "../user.service";
import {
  AlertDialogButtonConfig,
  AlertDialogComponent,
  AlertDialogData
} from "../../alert-dialog/alert-dialog.component";

export interface UserDetailsComponentData {
  userId?: number;
  userName?: string;
}

export interface UserDetailsComponentResponseData {
  user?: KokuDto.KokuUserDetailsDto;
}


@Component({
  selector: 'user-details',
  templateUrl: './user-details.component.html',
  styleUrls: ['./user-details.component.scss']
})
export class UserDetailsComponent {

  user: KokuDto.KokuUserDetailsDto | undefined;
  saving: boolean = false;
  loading: boolean = true;
  createMode: boolean;

  constructor(@Inject(MAT_DIALOG_DATA) public data: UserDetailsComponentData,
              public dialogRef: MatDialogRef<UserDetailsComponent>,
              public dialog: MatDialog,
              public userService: UserService) {
    this.createMode = data.userId === undefined;
    if (data.userId) {
      this.userService.getUser(data.userId).subscribe((user) => {
        this.user = user;
        this.loading = false;
      });
    } else {
      this.user = {
        username: this.data.userName || ''
      };
      this.loading = false;
    }
  }

  save(user: KokuDto.KokuUserDetailsDto, form: NgForm) {
    if (form.valid) {
      this.saving = true;
      if (!user.id) {
        this.userService.createUser(user).subscribe((result) => {
          const dialogResult: UserDetailsComponentResponseData = {
            user: result
          };
          this.dialogRef.close(dialogResult);
          this.saving = false;
        }, () => {
          this.saving = false;
        });
      } else {
        this.userService.updateUser(user).subscribe(() => {
          const dialogResult: UserDetailsComponentResponseData = {
            user
          };
          this.dialogRef.close(dialogResult);
          this.saving = false;
        }, () => {
          this.saving = false;
        });
      }
    }
  }

  delete(user: KokuDto.KokuUserDetailsDto) {
    const dialogData: AlertDialogData = {
      headline: 'Nutzer Löschen',
      message: `Wollen Sie den Nutzer mit dem Namen ${user.username} wirklich löschen?`,
      buttons: [{
        text: 'Abbrechen',
        onClick: (mouseEvent: Event, button: AlertDialogButtonConfig, dialogRef: MatDialogRef<AlertDialogComponent>) => {
          dialogRef.close();
        }
      }, {
        text: 'Bestätigen',
        onClick: (mouseEvent: Event, button: AlertDialogButtonConfig, dialogRef: MatDialogRef<AlertDialogComponent>) => {
          button.loading = true;
          this.userService.deleteUser(user).subscribe(() => {
            dialogRef.close();
            this.dialogRef.close();
          }, () => {
            button.loading = false;
          });
        }
      }]
    };

    this.dialog.open(AlertDialogComponent, {
      data: dialogData,
      width: '100%',
      maxWidth: 700,
      closeOnNavigation: false,
      position: {
        top: '20px'
      }
    });
  }

}

import {Injectable} from '@angular/core';
import {MatSnackBar} from "@angular/material/snack-bar";

@Injectable({
  providedIn: 'root'
})
export class SnackBarService {

  constructor(
    private readonly snackBar: MatSnackBar
  ) {
  }

  openCommonSnack(msg: string, position: 'top' | 'bottom' = 'bottom') {
    this.snackBar.open(msg,
      'ok',
      {
        duration: 5000,
        verticalPosition: position,
        politeness: "polite"
      }
    );
  }
}

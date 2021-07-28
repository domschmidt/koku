import {Injectable} from "@angular/core";
import {SwUpdate} from "@angular/service-worker";
import {SnackBarService} from "./snackbar/snack-bar.service";

@Injectable()
export class SwUpdateService {

  constructor(
    private readonly updates: SwUpdate,
    private readonly snackService: SnackBarService
  ) {
    updates.available.subscribe(() => {
      this.snackService.openCommonSnack("Update wird installiert...");
      updates.activateUpdate().then(() => document.location.reload());
    });
  }
}

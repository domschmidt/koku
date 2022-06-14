import {ApplicationRef, Injectable} from "@angular/core";
import {SwUpdate} from "@angular/service-worker";
import {SnackBarService} from "./snackbar/snack-bar.service";

@Injectable()
export class SwUpdateService {

  constructor(
    private readonly appRef: ApplicationRef,
    private readonly swUpdate: SwUpdate,
    private readonly snackService: SnackBarService
  ) {
    swUpdate.versionUpdates.subscribe((evt) => {
      window.location.reload();
      switch (evt.type) {
        case 'VERSION_DETECTED':
          this.snackService.openCommonSnack(`Update wird installiert...${evt.version.hash}`);
          break;
        case 'VERSION_READY':
          this.snackService.openCommonSnack(`Update wurde erfolgreich installiert...${evt.latestVersion.hash}`);
          document.location.reload();
          break;
        case 'VERSION_INSTALLATION_FAILED':
          this.snackService.openCommonSnack(`Update konnte nicht installiert werden...${evt.error}`);
          break;
      }
    });
  }
}

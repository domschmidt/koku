import { Injectable } from '@angular/core';
import { Observable } from 'rxjs';
import { CanDeactivate } from '@angular/router';

@Injectable({ providedIn: 'root' })
export class UnsavedChangesPreventionGuard<T> implements CanDeactivate<T> {
  unsavedChangesPreventionRegistry: Map<any, () => Observable<boolean>> = new Map<any, () => Observable<boolean>>();

  public registerUnsavedChangesPrevention(instance: any, shouldProceed: () => Observable<boolean>) {
    this.unsavedChangesPreventionRegistry.set(instance, shouldProceed);
  }

  public unregisterUnsavedChangesPrevention(instance: any) {
    this.unsavedChangesPreventionRegistry.delete(instance);
  }

  canDeactivate(): Observable<boolean> | boolean {
    return new Observable<boolean>((observer) => {
      const unsavedChangesPreventionRegistryEntries = this.unsavedChangesPreventionRegistry.values();
      const currentEntry = unsavedChangesPreventionRegistryEntries.next();
      if (currentEntry && currentEntry.value) {
        currentEntry.value().subscribe((shouldProceed) => {
          observer.next(shouldProceed);
          observer.complete();
        });
      } else {
        observer.next(true);
        observer.complete();
      }
    });
  }
}

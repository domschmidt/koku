class GlobalEvents {
  registeredListeners: Record<string, Record<string, (payload: any) => void>> = {};

  public addGlobalEventListener(uniqueComponentRef: string, eventName: string, cb: (payload: any) => void) {
    if (!this.registeredListeners[uniqueComponentRef]) {
      this.registeredListeners[uniqueComponentRef] = {};
    }
    this.registeredListeners[uniqueComponentRef][eventName] = cb;
  }

  public removeGlobalEventListener(uniqueComponentRef: string) {
    delete this.registeredListeners[uniqueComponentRef];
  }

  public propagateGlobalEvent(eventName: string, payload: any) {
    for (const entryValue of Object.values(this.registeredListeners)) {
      if (entryValue[eventName]) {
        entryValue[eventName](payload);
      }
    }
  }
}

export const GLOBAL_EVENT_BUS = new GlobalEvents();

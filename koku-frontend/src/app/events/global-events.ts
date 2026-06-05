type GlobalEventListener = (payload: any) => void;

export class GlobalEvents {
  private readonly listeners = new Map<string, Map<string, Set<GlobalEventListener>>>();

  public addGlobalEventListener(ownerId: string, eventName: string, listener: GlobalEventListener): () => void {
    const ownerListeners = this.listeners.get(ownerId) ?? new Map<string, Set<GlobalEventListener>>();
    const eventListeners = ownerListeners.get(eventName) ?? new Set<GlobalEventListener>();
    eventListeners.add(listener);
    ownerListeners.set(eventName, eventListeners);
    this.listeners.set(ownerId, ownerListeners);
    return () => this.removeListener(ownerId, eventName, listener);
  }

  public removeGlobalEventListener(ownerId: string) {
    this.listeners.delete(ownerId);
  }

  public propagateGlobalEvent(eventName: string, payload: any) {
    const errors: unknown[] = [];
    for (const ownerListeners of this.listeners.values()) {
      for (const listener of [...(ownerListeners.get(eventName) ?? [])]) {
        try {
          listener(payload);
        } catch (error) {
          errors.push(error);
        }
      }
    }
    if (errors.length > 0) {
      throw new AggregateError(errors, `Global event listener failed: ${eventName}`);
    }
  }

  private removeListener(ownerId: string, eventName: string, listener: GlobalEventListener) {
    const ownerListeners = this.listeners.get(ownerId);
    const eventListeners = ownerListeners?.get(eventName);
    eventListeners?.delete(listener);
    if (eventListeners?.size === 0) {
      ownerListeners?.delete(eventName);
    }
    if (ownerListeners?.size === 0) {
      this.listeners.delete(ownerId);
    }
  }
}

export const GLOBAL_EVENT_BUS = new GlobalEvents();

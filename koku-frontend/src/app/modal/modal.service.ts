import { Injectable, signal } from '@angular/core';
import { ModalType, RenderedModalType } from './modal.type';

@Injectable({
  providedIn: 'root',
})
export class ModalService {
  private modalUid = 0;
  renderedModals = signal<Record<number, RenderedModalType>>({});

  add(modal: ModalType) {
    const renderedModal: RenderedModalType = {
      ...modal,
      uid: this.modalUid++,
      close: () => {
        const oldModalInst = this.renderedModals()[renderedModal.uid];
        if (!oldModalInst) return;

        this.close(oldModalInst);
      },
      buttons: modal.buttons,
      update: (updatedModal) => {
        const oldModalInst = this.renderedModals()[renderedModal.uid];
        if (!oldModalInst) return;

        this.update(oldModalInst, updatedModal);
      },
    };
    this.renderedModals.set({ ...this.renderedModals(), [renderedModal.uid]: renderedModal });
    return renderedModal;
  }

  close(modal: RenderedModalType) {
    const modalsSnapshot = { ...this.renderedModals() };
    delete modalsSnapshot[modal.uid];
    this.renderedModals.set({ ...modalsSnapshot });
  }

  update(oldModal: RenderedModalType, newModal: ModalType) {
    const modalsSnapshot = { ...this.renderedModals() };
    modalsSnapshot[oldModal.uid] = {
      ...oldModal,
      ...newModal,
    };
    this.renderedModals.set(modalsSnapshot);
  }
}

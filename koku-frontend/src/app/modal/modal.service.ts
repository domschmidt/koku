import { Injectable, signal } from '@angular/core';
import { ModalType, RenderedModalType } from './modal.type';

@Injectable({
  providedIn: 'root',
})
export class ModalService {
  private modalUid = 0;
  readonly renderedModals = signal<readonly RenderedModalType[]>([]);

  add(modal: ModalType) {
    const renderedModal: RenderedModalType = {
      ...modal,
      uid: this.modalUid++,
      close: () => this.close(renderedModal),
      update: (updatedModal) => this.update(renderedModal, updatedModal),
    };
    this.renderedModals.update((modals) => [...modals, renderedModal]);
    return renderedModal;
  }

  close(modal: RenderedModalType) {
    this.renderedModals.update((modals) => modals.filter((currentModal) => currentModal.uid !== modal.uid));
  }

  update(oldModal: RenderedModalType, newModal: ModalType) {
    this.renderedModals.update((modals) =>
      modals.map((currentModal) =>
        currentModal.uid === oldModal.uid ? { ...currentModal, ...newModal } : currentModal,
      ),
    );
  }
}

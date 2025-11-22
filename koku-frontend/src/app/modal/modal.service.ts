import {Injectable, signal} from '@angular/core';
import {ModalButtonType, ModalType, RenderedModalType} from './modal.type';

@Injectable({
  providedIn: 'root'
})
export class ModalService {

  private modalUid = 0;
  private buttonUid = 0;
  renderedModals = signal<RenderedModalType[]>([]);

  add(modal: ModalType) {
    const newModal: RenderedModalType = {
      ...modal,
      uid: this.modalUid++,
      close: () => {
        this.close(newModal);
      },
      buttons: (modal.buttons || []).map((button: ModalButtonType) => {
        return {
          ...button,
          'uid': this.buttonUid++
        };
      })
    };
    this.renderedModals.set([...this.renderedModals(), newModal]);
    return newModal;
  }

  close(modal: RenderedModalType) {
    const modalsSnapshot = this.renderedModals();
    const idx = modalsSnapshot.indexOf(modal);
    if (idx >= 0) {
      modalsSnapshot.splice(idx, 1);
      this.renderedModals.set([...modalsSnapshot]);
    }
  }

  update(oldModal: RenderedModalType, newModal: ModalType) {
    const modalsSnapshot = this.renderedModals();
    const idx = modalsSnapshot.indexOf(oldModal);
    if (idx >= 0) {
      modalsSnapshot[idx] = {
        ...newModal,
        uid: oldModal.uid,
        close: () => {
          this.close(oldModal);
        },
        buttons: (newModal.buttons || []).map((button: ModalButtonType) => {
          return {
            ...button,
            'uid': this.buttonUid++
          };
        })
      }
      this.renderedModals.set([...modalsSnapshot]);
    }
  }

}

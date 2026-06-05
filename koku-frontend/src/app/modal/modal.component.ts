import { Component, inject } from '@angular/core';
import { ModalService } from './modal.service';
import { ButtonComponent } from '../button/button.component';
import { ModalType } from './modal.type';
import { ModalContentRendererComponent } from './modal-content-renderer.component';

@Component({
  selector: 'koku-modal',
  imports: [ButtonComponent, ModalContentRendererComponent],
  templateUrl: './modal.component.html',
  styles: ':host { position: relative; z-index: 999; }',
})
export class ModalComponent {
  readonly modalService = inject(ModalService);

  handleBackdropClick(event: Event, modal: ModalType) {
    if (event.target === event.currentTarget) {
      if (modal.clickOutside) {
        modal.clickOutside(event);
      }
    }
  }
}

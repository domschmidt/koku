import { TestBed } from '@angular/core/testing';
import { ModalComponent } from './modal.component';
import { ModalService } from './modal.service';
import { vi } from 'vitest';

describe('ModalComponent', () => {
  it('keeps insertion order and raises each new modal above the previous one', () => {
    const fixture = TestBed.createComponent(ModalComponent);
    const modalService = TestBed.inject(ModalService);
    for (let index = 0; index < 12; index++) {
      modalService.add({ headline: `Modal ${index}` });
    }

    fixture.detectChanges();

    const dialogs = Array.from(fixture.nativeElement.querySelectorAll('dialog')) as HTMLDialogElement[];
    expect(dialogs.map((dialog) => dialog.textContent?.trim())).toEqual(
      Array.from({ length: 12 }, (_, index) => `Modal ${index}`),
    );
    expect(dialogs.map((dialog) => dialog.style.zIndex)).toEqual(
      Array.from({ length: 12 }, (_, index) => String(index)),
    );
  });

  it('updates and closes a modal without changing the remaining stack order', () => {
    const modalService = TestBed.inject(ModalService);
    const first = modalService.add({ headline: 'First' });
    const second = modalService.add({ headline: 'Second' });
    const third = modalService.add({ headline: 'Third' });

    second.update({ headline: 'Updated' });
    first.close();

    expect(modalService.renderedModals().map((modal) => modal.uid)).toEqual([second.uid, third.uid]);
    expect(modalService.renderedModals().map((modal) => modal.headline)).toEqual(['Updated', 'Third']);
  });

  it('delegates backdrop clicks only when the modal configured a handler', () => {
    const fixture = TestBed.createComponent(ModalComponent);
    const event = { target: fixture.nativeElement, currentTarget: fixture.nativeElement } as unknown as Event;
    const clickOutside = vi.fn();

    fixture.componentInstance.handleBackdropClick(event, { clickOutside } as any);
    fixture.componentInstance.handleBackdropClick(event, {} as any);
    fixture.componentInstance.handleBackdropClick(
      { target: {}, currentTarget: fixture.nativeElement } as unknown as Event,
      { clickOutside } as any,
    );

    expect(clickOutside).toHaveBeenCalledOnce();
  });
});

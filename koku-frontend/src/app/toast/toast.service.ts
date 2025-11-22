import {Injectable, signal} from '@angular/core';

type ToastButtonType = {
  uid: number;
  text: string;
  title: string;
  loading?: boolean;
  size?: 'sm';
  type?: 'primary' | 'secondary' | 'accent' | 'info' | 'success' | 'warning' | 'error';
  progressAnimationDurationMillis?: number;
  onClick: (event: Event, toast: ToastType, button: ToastButtonType) => void;
};

export type ToastTypeUnion = 'outline' | 'dash' | 'soft' | 'info' | 'success' | 'warning' | 'error';

interface ToastType {
  uid: number;
  message: string;
  type: ToastTypeUnion,
  close: () => void,
  buttons: ToastButtonType[]
}

const DEFAULT_DURATION_MS = 5000;

@Injectable({
  providedIn: 'root'
})
export class ToastService {

  private toastUid = 0;
  private buttonUid = 0;
  values = signal<ToastType[]>([]);

  add(message: string, type: ToastTypeUnion = 'success', buttons?: ToastButtonType[], durationMillis?: number) {
    const defaultCloseButton: ToastButtonType = {
      title: "Schließen",
      text: "Schließen",
      uid: this.buttonUid++,
      progressAnimationDurationMillis: durationMillis === undefined ? DEFAULT_DURATION_MS : undefined,
      type: "primary",
      onClick: (event: Event, toast: ToastType, button: ToastButtonType) => {
        toast.close();
      },
    };
    const newToast = {
      uid: this.toastUid++,
      message,
      type,
      buttons: buttons || [defaultCloseButton],
      close: () => {
        this.close(newToast);
      }
    };
    this.values.set([...this.values(), newToast]);
    if (durationMillis === undefined) {
      setTimeout(() => newToast.close(), DEFAULT_DURATION_MS);
    }
  }

  close(toast: ToastType) {
    const toastSnapshot = this.values();
    const idx = toastSnapshot.indexOf(toast);
    if (idx >= 0) {
      toastSnapshot.splice(idx, 1);
      this.values.set([...toastSnapshot]);
    }
  }

}

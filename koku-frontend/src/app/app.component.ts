import { ChangeDetectionStrategy, Component, inject } from '@angular/core';
import { RouterOutlet } from '@angular/router';
import { ModalComponent } from './modal/modal.component';
import { ThemingService } from './theme/theming.service';
import { ToastComponent } from './toast/toast.component';

@Component({
  selector: 'koku-root',
  imports: [RouterOutlet, ToastComponent, ModalComponent],
  templateUrl: './app.component.html',
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class AppComponent {
  private readonly themingService = inject(ThemingService);

  theme: string;

  constructor() {
    const themingService = this.themingService;

    this.themingService.theme.subscribe((theme: string) => {
      this.theme = theme;
    });
    this.theme = themingService.theme.value;
  }
}

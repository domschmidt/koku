import { Component, input, output } from '@angular/core';
import { IconComponent } from '../../icon/icon.component';
import { AvatarComponent } from '../../avatar/avatar.component';

@Component({
  selector: 'calendar-action-button',
  imports: [IconComponent, AvatarComponent],
  templateUrl: './calendar-action-button.component.html',
})
export class CalendarActionButtonComponent {
  title = input<string>();
  loading = input(false);
  icon = input<string>();
  imgBase64 = input<string>();

  clicked = output<Event>();
}

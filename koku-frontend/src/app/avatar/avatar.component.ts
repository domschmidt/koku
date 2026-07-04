import { booleanAttribute, Component, input } from '@angular/core';

@Component({
  selector: 'koku-avatar',
  host: { class: 'order-[-1] inline-flex' },
  imports: [],
  templateUrl: './avatar.component.html',
})
export class AvatarComponent {
  value = input<string>();
  loading = input(false, { transform: booleanAttribute });
}

import { booleanAttribute, Component, input } from '@angular/core';

@Component({
  selector: 'koku-text-circle',
  host: { class: 'order-[-1] inline-flex' },
  imports: [],
  templateUrl: './text-circle.component.html',
})
export class TextCircleComponent {
  value = input<string>();
  loading = input(false, { transform: booleanAttribute });
}

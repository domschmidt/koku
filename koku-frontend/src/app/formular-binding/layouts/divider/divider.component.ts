import { booleanAttribute, Component, input } from '@angular/core';

@Component({
  selector: 'divider',
  imports: [],
  templateUrl: './divider.component.html',
})
export class DividerComponent {
  loading = input(false, { transform: booleanAttribute });
  disabled = input(false, { transform: booleanAttribute });
  text = input<string>();
}

import { Component, input, output } from '@angular/core';
import { IconComponent } from '../icon/icon.component';

export interface DockContentItem {
  id: string;
  active?: boolean;
  icon?: string;
  title?: string;
}

@Component({
  selector: 'koku-dock',
  host: { class: 'flex h-full max-h-full w-full flex-col overflow-auto' },
  imports: [IconComponent],
  templateUrl: './dock.component.html',
})
export class DockComponent {
  content = input.required<DockContentItem[]>();
  activationRequested = output<DockContentItem>();
}

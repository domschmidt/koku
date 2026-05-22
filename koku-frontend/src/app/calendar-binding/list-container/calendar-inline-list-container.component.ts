import { Component, input, output } from '@angular/core';
import { ListComponent, ListContentSetup } from '../../list/list.component';

@Component({
  selector: '[calendar-inline-list-container],calendar-inline-list-container',
  imports: [ListComponent],
  templateUrl: './calendar-inline-list-container.component.html',
  styleUrl: './calendar-inline-list-container.component.css',
})
export class CalendarInlineListContainerComponent {
  title = input<string>();
  listUrl = input<string>();
  sourceUrl = input<string>();
  urlSegments = input<Record<string, string> | null>(null);
  contentSetup = input.required<ListContentSetup>();
  parentRoutePath = input<string>('');

  closeRequested = output<void>();
  openRoutedContentRequested = output<string[]>();

  closeInlineContent() {
    this.closeRequested.emit();
  }

  protected readonly Object = Object;
}

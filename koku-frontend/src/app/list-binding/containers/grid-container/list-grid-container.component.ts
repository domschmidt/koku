import { Component, input, output } from '@angular/core';
import { ListContentSetup } from '../../../list/list.component';
import { ListInlineContentComponent } from '../../../list/list-inline-content/list-inline-content.component';

@Component({
  selector: '[list-inline-grid-container],list-inline-grid-container',
  host: { class: 'flex w-full flex-col overflow-auto' },
  imports: [ListInlineContentComponent],
  templateUrl: './list-grid-container.component.html',
})
export class ListGridContainerComponent {
  content = input.required<KokuDto.ListViewGridContentDto>();
  contentSetup = input.required<ListContentSetup>();
  urlSegments = input<Record<string, string> | null>(null);
  parentRoutePath = input<string>('');

  closeRequested = output<void>();
  openRoutedContentRequested = output<string[]>();

  closeInlineContent() {
    this.closeRequested.emit();
  }

  openRoutedContent(routes: string[]) {
    this.openRoutedContentRequested.emit(routes);
  }
}

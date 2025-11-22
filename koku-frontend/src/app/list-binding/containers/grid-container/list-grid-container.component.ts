import {Component, input, output} from '@angular/core';
import {ListContentSetup} from '../../../list/list.component';
import {ListInlineContentComponent} from '../../../list/list-inline-content/list-inline-content.component';

@Component({
  selector: '[list-inline-grid-container],list-inline-grid-container',
  imports: [ListInlineContentComponent
  ],
  templateUrl: './list-grid-container.component.html',
  styleUrl: './list-grid-container.component.css'
})
export class ListGridContainerComponent {

  content = input.required<KokuDto.ListViewGridContentDto>();
  contentSetup = input.required<ListContentSetup>();
  urlSegments = input<{ [key: string]: string } | null>(null);
  parentRoutePath = input<string>('');

  onClose = output<void>();
  onOpenRoutedContent = output<string[]>();

  closeInlineContent() {
    this.onClose.emit();
  }

  openRoutedContent(routes: string[]) {
    this.onOpenRoutedContent.emit(routes);
  }

}

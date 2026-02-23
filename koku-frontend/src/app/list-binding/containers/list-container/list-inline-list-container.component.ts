import { Component, DestroyRef, inject, input, output, signal } from '@angular/core';
import { ListComponent, ListContentSetup } from '../../../list/list.component';
import { takeUntilDestroyed, toObservable } from '@angular/core/rxjs-interop';
import { HttpClient } from '@angular/common/http';
import { forkJoin, map, Observable } from 'rxjs';

@Component({
  selector: '[list-inline-list-container],list-inline-list-container',
  imports: [ListComponent],
  templateUrl: './list-inline-list-container.component.html',
  styleUrl: './list-inline-list-container.component.css',
})
export class ListInlineListContainerComponent {
  title = input<string>();
  listUrl = input<string>();
  sourceUrl = input<string>();
  urlSegments = input<Record<string, string> | null>(null);
  contentSetup = input.required<ListContentSetup>();
  parentRoutePath = input<string>('');
  contextMapping = input<Record<string, KokuDto.AbstractListViewListContentContextDto>>();

  mappedContext = signal<Record<string, any> | null | undefined>(undefined);

  onClose = output<void>();
  onOpenRoutedContent = output<string[]>();

  destroyRef = inject(DestroyRef);
  httpClient = inject(HttpClient);

  constructor() {
    toObservable(this.contextMapping)
      .pipe(takeUntilDestroyed(this.destroyRef))
      .subscribe((value) => {
        const resolvedContext: Record<string, any> = {};
        if (value) {
          const observables: Observable<any>[] = [];
          for (const [currentKey, currentValue] of Object.entries(value || {})) {
            switch (currentValue['@type']) {
              case 'endpoint': {
                const castedValue = currentValue as KokuDto.EndpointListViewListContentContextDto;
                if (!castedValue.endpointMethod) {
                  throw new Error('Missing endpoint method');
                }
                if (!castedValue.endpointUrl) {
                  throw new Error('Missing endpoint url');
                }

                let endpointUrl = castedValue.endpointUrl;
                for (const [segment, value] of Object.entries(this.urlSegments() || {})) {
                  endpointUrl = endpointUrl.replace(segment, value);
                }

                observables.push(
                  this.httpClient.request(castedValue.endpointMethod, endpointUrl).pipe(
                    map((response) => {
                      resolvedContext[currentKey] = response;
                    }),
                  ),
                );
                break;
              }
              default: {
                throw new Error(`Unknown context type ${currentValue['@type']}`);
              }
            }
          }
          if (observables.length > 0) {
            forkJoin(observables).subscribe({
              next: () => {
                this.mappedContext.set(resolvedContext);
              },
            });
          } else {
            this.mappedContext.set(null);
          }
        } else {
          this.mappedContext.set(null);
        }
      });
  }

  closeInlineContent() {
    this.onClose.emit();
  }
}

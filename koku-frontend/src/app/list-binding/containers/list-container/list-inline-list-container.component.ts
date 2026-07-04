import { Component, DestroyRef, inject, input, output, signal } from '@angular/core';
import { ListComponent, ListContentSetup } from '../../../list/list.component';
import { takeUntilDestroyed, toObservable } from '@angular/core/rxjs-interop';
import { HttpClient } from '@angular/common/http';
import { forkJoin, map, Observable } from 'rxjs';

@Component({
  selector: '[list-inline-list-container],list-inline-list-container',
  host: { class: 'flex h-full w-full flex-col overflow-auto' },
  imports: [ListComponent],
  templateUrl: './list-inline-list-container.component.html',
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

  closeRequested = output<void>();
  openRoutedContentRequested = output<string[]>();

  destroyRef = inject(DestroyRef);
  httpClient = inject(HttpClient);

  constructor() {
    toObservable(this.contextMapping)
      .pipe(takeUntilDestroyed(this.destroyRef))
      .subscribe((value) => {
        this.resolveContextMapping(value);
      });
  }

  private resolveContextMapping(
    contextMapping: Record<string, KokuDto.AbstractListViewListContentContextDto> | undefined,
  ): void {
    if (!contextMapping) {
      this.mappedContext.set(null);
      return;
    }

    const resolvedContext: Record<string, any> = {};
    const observables = this.createContextRequests(contextMapping, resolvedContext);
    if (observables.length === 0) {
      this.mappedContext.set(null);
      return;
    }

    forkJoin(observables).subscribe({
      next: () => {
        this.mappedContext.set(resolvedContext);
      },
    });
  }

  private createContextRequests(
    contextMapping: Record<string, KokuDto.AbstractListViewListContentContextDto>,
    resolvedContext: Record<string, any>,
  ): Observable<any>[] {
    return Object.entries(contextMapping).map(([currentKey, currentValue]) =>
      this.createContextRequest(currentKey, currentValue, resolvedContext),
    );
  }

  private createContextRequest(
    currentKey: string,
    currentValue: KokuDto.AbstractListViewListContentContextDto,
    resolvedContext: Record<string, any>,
  ): Observable<any> {
    if (currentValue['@type'] !== 'endpoint') {
      throw new Error(`Unknown context type ${currentValue['@type']}`);
    }

    const castedValue = currentValue as KokuDto.EndpointListViewListContentContextDto;
    if (!castedValue.endpointMethod) {
      throw new Error('Missing endpoint method');
    }
    if (!castedValue.endpointUrl) {
      throw new Error('Missing endpoint url');
    }

    return this.httpClient.request(castedValue.endpointMethod, this.resolveEndpointUrl(castedValue.endpointUrl)).pipe(
      map((response) => {
        resolvedContext[currentKey] = response;
      }),
    );
  }

  private resolveEndpointUrl(endpointUrl: string): string {
    let resolvedEndpointUrl = endpointUrl;
    for (const [segment, value] of Object.entries(this.urlSegments() || {})) {
      resolvedEndpointUrl = resolvedEndpointUrl.replace(segment, value);
    }
    return resolvedEndpointUrl;
  }

  closeInlineContent() {
    this.closeRequested.emit();
  }
}

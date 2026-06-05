import { booleanAttribute, Component, inject, input, output } from '@angular/core';
import { OutletDirective } from '../../portal/outlet.directive';
import { CalendarContentSetup } from '../../calendar/calendar.component';
import { ActivatedRoute } from '@angular/router';
import { KokuDynamicHostDirective } from '../../dynamic-host/dynamic-host.directive';
import { toSignal } from '@angular/core/rxjs-interop';
import { createStableRecipe, requireRecipeFactory } from '../../dynamic-host/recipe.utils';

@Component({
  selector: '[calendar-inline-content],calendar-inline-content',
  imports: [KokuDynamicHostDirective],
  templateUrl: './calendar-inline-content.component.html',
  styleUrl: './calendar-inline-content.component.css',
})
export class CalendarInlineContentComponent {
  activatedRoute = inject(ActivatedRoute);

  content = input.required<KokuDto.AbstractCalendarInlineContentDto>();
  loading = input(false, { transform: booleanAttribute });
  contentSetup = input.required<CalendarContentSetup>();
  urlSegments = input<Record<string, string> | null>(null);
  buttonDockOutlet = input<OutletDirective>();
  parentRoutePath = input<string>('');

  closeRequested = output<void>();
  openRoutedContentRequested = output<string[]>();

  queryParams = toSignal(this.activatedRoute.queryParams, {
    initialValue: this.activatedRoute.snapshot.queryParams,
  });

  inlineContentRecipe = createStableRecipe({
    identity: () => {
      const content = this.content();
      return {
        content,
        factory: requireRecipeFactory(
          this.contentSetup().inlineContentRegistry,
          content['@type'],
          'calendar inline content',
        ),
      };
    },
    equal: (previous, current) => previous.content === current.content && previous.factory === current.factory,
    create: ({ factory }) =>
      factory({
        content: this.content,
        loading: this.loading,
        contentSetup: this.contentSetup,
        urlSegments: this.urlSegments,
        buttonDockOutlet: this.buttonDockOutlet,
        parentRoutePath: this.parentRoutePath,
        queryParams: this.queryParams,
        close: () => this.closeInlineContent(),
        openRoutedContent: (routes: string[]) => this.openRoutedContent(routes),
      }),
  });

  closeInlineContent() {
    this.closeRequested.emit();
  }

  openRoutedContent(routes: string[]) {
    this.openRoutedContentRequested.emit(routes);
  }
}

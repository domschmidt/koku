import { booleanAttribute, Component, input, output } from '@angular/core';
import { ListContentSetup } from '../list.component';
import { OutletDirective } from '../../portal/outlet.directive';
import { KokuDynamicHostDirective } from '../../dynamic-host/dynamic-host.directive';
import { createStableRecipe, requireRecipeFactory } from '../../dynamic-host/recipe.utils';

@Component({
  selector: '[list-inline-content],list-inline-content',
  imports: [KokuDynamicHostDirective],
  templateUrl: './list-inline-content.component.html',
  styleUrl: './list-inline-content.component.css',
})
export class ListInlineContentComponent {
  content = input.required<KokuDto.AbstractListViewContentDto>();
  loading = input(false, { transform: booleanAttribute });
  contentSetup = input.required<ListContentSetup>();
  urlSegments = input<Record<string, string> | null>(null);
  parentRoutePath = input<string>('');
  buttonDockOutlet = input<OutletDirective>();
  context = input<Record<string, any>>();

  closeRequested = output<void>();
  openRoutedContentRequested = output<string[]>();

  inlineContentRecipe = createStableRecipe({
    identity: () => {
      const content = this.content();
      return {
        content,
        factory: requireRecipeFactory(
          this.contentSetup().inlineContentRegistry,
          content['@type'],
          'list inline content',
        ),
      };
    },
    equal: (previous, current) => previous.content === current.content && previous.factory === current.factory,
    create: ({ factory }) =>
      factory({
        content: this.content,
        loading: this.loading,
        urlSegments: this.urlSegments,
        parentRoutePath: this.parentRoutePath,
        buttonDockOutlet: this.buttonDockOutlet,
        context: this.context,
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

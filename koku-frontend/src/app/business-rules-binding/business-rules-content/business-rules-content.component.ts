import { booleanAttribute, Component, input, output } from '@angular/core';
import { OutletDirective } from '../../portal/outlet.directive';
import { BusinessRulesContentRegistry } from '../registry';
import { KokuDynamicHostDirective } from '../../dynamic-host/dynamic-host.directive';
import { createStableRecipe, requireRecipeFactory } from '../../dynamic-host/recipe.utils';

@Component({
  selector: '[business-rules-content],business-rules-content',
  host: { class: 'h-full' },
  imports: [KokuDynamicHostDirective],
  templateUrl: './business-rules-content.component.html',
})
export class BusinessRulesContentComponent {
  content = input.required<KokuDto.AbstractKokuBusinessRuleContentDto>();
  loading = input(false, { transform: booleanAttribute });
  contentSetup = input.required<BusinessRulesContentRegistry>();
  urlSegments = input<Record<string, string> | null>(null);
  parentRoutePath = input<string>('');
  buttonDockOutlet = input<OutletDirective>();

  closeRequested = output<void>();
  openRoutedContentRequested = output<string[]>();

  contentRecipe = createStableRecipe({
    identity: () => {
      const content = this.content();
      return {
        content,
        factory: requireRecipeFactory(this.contentSetup().contentRegistry, content['@type'], 'business-rule content'),
      };
    },
    equal: (previous, current) => previous.content === current.content && previous.factory === current.factory,
    create: ({ factory }) =>
      factory({
        content: this.content,
        loading: this.loading,
        contentSetup: this.contentSetup,
        urlSegments: this.urlSegments,
        parentRoutePath: this.parentRoutePath,
        buttonDockOutlet: this.buttonDockOutlet,
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

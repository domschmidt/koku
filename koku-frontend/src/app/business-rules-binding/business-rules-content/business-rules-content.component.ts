import { booleanAttribute, Component, input, output } from '@angular/core';
import { SignalComponentIoModule } from 'ng-dynamic-component/signal-component-io';
import { ComponentOutletInjectorModule, DynamicComponent, DynamicIoDirective } from 'ng-dynamic-component';
import { OutletDirective } from '../../portal/outlet.directive';
import { BusinessRulesContentRegistry } from '../registry';
import { KokuBusinessRuleContent } from '../../../types/generated/business-logic';

@Component({
  selector: '[business-rules-content],business-rules-content',
  imports: [SignalComponentIoModule, DynamicIoDirective, ComponentOutletInjectorModule, DynamicComponent],
  templateUrl: './business-rules-content.component.html',
  styleUrl: './business-rules-content.component.css',
})
export class BusinessRulesContentComponent {
  content = input.required<KokuBusinessRuleContent>();
  loading = input(false, { transform: booleanAttribute });
  contentSetup = input.required<BusinessRulesContentRegistry>();
  urlSegments = input<Record<string, string> | null>(null);
  parentRoutePath = input<string>('');
  buttonDockOutlet = input<OutletDirective>();

  closeRequested = output<void>();
  openRoutedContentRequested = output<string[]>();

  closeInlineContent() {
    this.closeRequested.emit();
  }

  openRoutedContent(routes: string[]) {
    this.openRoutedContentRequested.emit(routes);
  }
}

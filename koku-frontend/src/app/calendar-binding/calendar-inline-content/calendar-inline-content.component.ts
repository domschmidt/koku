import { booleanAttribute, Component, inject, input, output } from '@angular/core';
import { SignalComponentIoModule } from 'ng-dynamic-component/signal-component-io';
import { ComponentOutletInjectorModule, DynamicComponent, DynamicIoDirective } from 'ng-dynamic-component';
import { OutletDirective } from '../../portal/outlet.directive';
import { CalendarContentSetup } from '../../calendar/calendar.component';
import { ActivatedRoute } from '@angular/router';

@Component({
  selector: '[calendar-inline-content],calendar-inline-content',
  imports: [SignalComponentIoModule, DynamicIoDirective, ComponentOutletInjectorModule, DynamicComponent],
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

  onClose = output<void>();
  onOpenRoutedContent = output<string[]>();

  closeInlineContent() {
    this.onClose.emit();
  }

  openRoutedContent(routes: string[]) {
    this.onOpenRoutedContent.emit(routes);
  }
}

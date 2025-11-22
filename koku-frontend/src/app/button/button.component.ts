import {booleanAttribute, Component, computed, input, output} from '@angular/core';
import {IconComponent} from '../icon/icon.component';

@Component({
  selector: 'koku-button',
  imports: [
    IconComponent
  ],
  templateUrl: './button.component.html',
  styleUrl: './button.component.css'
})
export class ButtonComponent {

  join = input(false, {transform: booleanAttribute});
  loading = input(false, {transform: booleanAttribute});
  disabled = input(false, {transform: booleanAttribute});
  href = input<string>();
  hrefTarget = input<KokuDto.EnumLinkTarget>();
  buttonType = input<KokuDto.EnumButtonType>();
  title = input<string>();
  icon = input<string>();
  text = input<string>();
  styles = input<("NEUTRAL" | "PRIMARY" | "SECONDARY" | "ACCENT" | "INFO" | "SUCCESS" | "WARNING" | "ERROR" | "OUTLINE" | "DASH" | "SOFT" | "GHOST" | "LINK" | "ACTIVE" | "DISABLED" | "WIDE" | "BLOCK" | "SQUARE" | "CIRCLE")[]>([]);
  size = input<"XS" | "SM" | "MD" | "LG" | "XL" | null>(null);

  indexedStyles = computed(() => new Set(this.styles()));

  onClick = output<Event>();
  onBlur = output<Event>();
  onFocus = output<Event>();

  onClickRaw(event: Event) {
    this.onClick.emit(event);
    const hrefSnapshot = this.href();
    const hrefTargetSnapshot = this.hrefTarget();
    if (hrefSnapshot) {
      window.open(hrefSnapshot, this.getLinkTarget(hrefTargetSnapshot))
    }
  }

  private getLinkTarget(hrefTarget?: KokuDto.EnumLinkTarget) {
    let result: string | undefined = undefined;

    if (hrefTarget === 'SELF') {
      result = "_self";
    } else if (hrefTarget === 'BLANK') {
      result = "_blank";
    }

    return result;
  }
}

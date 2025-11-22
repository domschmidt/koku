import {booleanAttribute, Component, input} from '@angular/core';

@Component({
  selector: 'koku-text-circle',
  imports: [],
  templateUrl: './text-circle.component.html',
  styleUrl: './text-circle.component.css'
})
export class TextCircleComponent {

  value = input<string>()
  loading = input(false, {transform: booleanAttribute});

}

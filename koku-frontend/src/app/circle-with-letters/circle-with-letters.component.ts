import {Component, Input} from '@angular/core';


@Component({
  selector: 'circle-with-letters',
  templateUrl: './circle-with-letters.component.html',
  styleUrls: ['./circle-with-letters.component.scss']
})
export class CircleWithLettersComponent {

  @Input('letters') letters: string | undefined;
  @Input('small') small: boolean | undefined;
  @Input('large') large: boolean | undefined;

  constructor() {
  }

}

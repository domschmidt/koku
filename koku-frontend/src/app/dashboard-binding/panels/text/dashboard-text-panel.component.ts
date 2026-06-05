import { Component, computed, input } from '@angular/core';
import { colorValue } from '../../../utils/color.utils';

type DashboardTextPanelView = KokuDto.DashboardTextPanelDto & {
  _color?: string;
  progressDetails?: (KokuDto.DashboardTextPanelProgressDetailsDto & { _headlineColor?: string })[];
};

@Component({
  selector: 'dashboard-text-panel',
  imports: [],
  templateUrl: './dashboard-text-panel.component.html',
})
export class DashboardTextPanelComponent {
  content = input.required<KokuDto.DashboardTextPanelDto>();

  contentWithColors = computed<DashboardTextPanelView>(() => {
    const content = this.content();
    return {
      ...content,
      _color: colorValue(content.color),
      progressDetails: content.progressDetails?.map((detail) => ({
        ...detail,
        _headlineColor: colorValue(detail.headlineColor),
      })),
    };
  });
}

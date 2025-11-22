import {Component, computed, input} from '@angular/core';

@Component({
  selector: 'dashboard-text-panel',
  imports: [],
  templateUrl: './dashboard-text-panel.component.html',
  styleUrl: './dashboard-text-panel.component.css'
})
export class DashboardTextPanelComponent {

  content = input.required<KokuDto.DashboardTextPanelDto>();
  contentWithColors = computed(() => {
    const contentSnapshot = this.content();
    let result: KokuDto.DashboardTextPanelDto & {
      _cardColor?: string;
      _topHeadlineColor?: string;
      _headlineColor?: string;
      _subHeadlineColor?: string;
      _progressColor?: string;
      _progressBarColor?: string;
      progressDetails?: KokuDto.DashboardTextPanelProgressDetailsDto & {
        _headlineColor?: string;
      } []
    } = {
      ...contentSnapshot
    }
    switch (contentSnapshot.color) {
      case 'PRIMARY': {
        result = {
          ...contentSnapshot,
          _cardColor: 'bg-primary-50 dark:bg-primary-900',
          _topHeadlineColor: 'text-primary-600 dark:text-primary-300',
          _headlineColor: 'text-primary-600 dark:text-primary-300',
          _subHeadlineColor: 'text-primary-500 dark:text-primary-300',
          _progressColor: 'bg-primary-200 dark:bg-primary-700',
          _progressBarColor: 'bg-primary-600 dark:bg-primary-400',
          progressDetails: contentSnapshot.progressDetails?.map(value => {
            return {
              ...value,
              _headlineColor: this.getProgressDetailHeadlineColor(value.headlineColor),
            };
          })
        };
        break;
      }
      case 'SECONDARY': {
        result = {
          ...contentSnapshot,
          _cardColor: 'bg-secondary-50 dark:bg-secondary-900',
          _topHeadlineColor: 'text-secondary-600 dark:text-secondary-300',
          _headlineColor: 'text-secondary-600 dark:text-secondary-300',
          _subHeadlineColor: 'text-secondary-500 dark:text-secondary-300',
          _progressColor: 'bg-secondary-200 dark:bg-secondary-700',
          _progressBarColor: 'bg-secondary-600 dark:bg-secondary-400',
          progressDetails: contentSnapshot.progressDetails?.map(value => {
            return {
              ...value,
              _headlineColor: this.getProgressDetailHeadlineColor(value.headlineColor),
            };
          })
        };
        break;
      }
      case 'ACCENT': {
        result = {
          ...contentSnapshot,
          _cardColor: 'bg-accent-50 dark:bg-accent-900',
          _topHeadlineColor: 'text-accent-600 dark:text-accent-300',
          _headlineColor: 'text-accent-600 dark:text-accent-300',
          _subHeadlineColor: 'text-accent-500 dark:text-accent-300',
          _progressColor: 'bg-accent-200 dark:bg-accent-700',
          _progressBarColor: 'bg-accent-600 dark:bg-accent-400',
          progressDetails: contentSnapshot.progressDetails?.map(value => {
            return {
              ...value,
              _headlineColor: this.getProgressDetailHeadlineColor(value.headlineColor),
            };
          })
        };
        break;
      }
      case 'INFO': {
        result = {
          ...contentSnapshot,
          _cardColor: 'bg-info-50 dark:bg-info-900',
          _topHeadlineColor: 'text-info-600 dark:text-info-300',
          _headlineColor: 'text-info-600 dark:text-info-300',
          _subHeadlineColor: 'text-info-500 dark:text-info-300',
          _progressColor: 'bg-info-200 dark:bg-info-700',
          _progressBarColor: 'bg-info-600 dark:bg-info-400',
          progressDetails: contentSnapshot.progressDetails?.map(value => {
            return {
              ...value,
              _headlineColor: this.getProgressDetailHeadlineColor(value.headlineColor),
            };
          })
        };
        break;
      }
      case 'SUCCESS': {
        result = {
          ...contentSnapshot,
          _cardColor: 'bg-success-50 dark:bg-success-900',
          _topHeadlineColor: 'text-success-600 dark:text-success-300',
          _headlineColor: 'text-success-600 dark:text-success-300',
          _subHeadlineColor: 'text-success-500 dark:text-success-300',
          _progressColor: 'bg-success-200 dark:bg-success-700',
          _progressBarColor: 'bg-success-600 dark:bg-success-400',
          progressDetails: contentSnapshot.progressDetails?.map(value => {
            return {
              ...value,
              _headlineColor: this.getProgressDetailHeadlineColor(value.headlineColor),
            };
          })
        };
        break;
      }
      case 'WARNING': {
        result = {
          ...contentSnapshot,
          _cardColor: 'bg-warning-50 dark:bg-warning-900',
          _topHeadlineColor: 'text-warning-600 dark:text-warning-300',
          _headlineColor: 'text-warning-600 dark:text-warning-300',
          _subHeadlineColor: 'text-warning-500 dark:text-warning-300',
          _progressColor: 'bg-warning-200 dark:bg-warning-700',
          _progressBarColor: 'bg-warning-600 dark:bg-warning-400',
          progressDetails: contentSnapshot.progressDetails?.map(value => {
            return {
              ...value,
              _headlineColor: this.getProgressDetailHeadlineColor(value.headlineColor),
            };
          })
        };
        break;
      }
      case 'ERROR': {
        result = {
          ...contentSnapshot,
          _cardColor: 'bg-error-50 dark:bg-error-900',
          _topHeadlineColor: 'text-error-600 dark:text-error-300',
          _headlineColor: 'text-error-600 dark:text-error-300',
          _subHeadlineColor: 'text-error-500 dark:text-error-300',
          _progressColor: 'bg-error-200 dark:bg-error-700',
          _progressBarColor: 'bg-error-600 dark:bg-error-400',
          progressDetails: contentSnapshot.progressDetails?.map(value => {
            return {
              ...value,
              _headlineColor: this.getProgressDetailHeadlineColor(value.headlineColor),
            };
          })
        };
        break;
      }
      case 'RED': {
        result = {
          ...contentSnapshot,
          _cardColor: 'bg-red-50 dark:bg-red-900',
          _topHeadlineColor: 'text-red-600 dark:text-red-300',
          _headlineColor: 'text-red-600 dark:text-red-300',
          _subHeadlineColor: 'text-red-500 dark:text-red-300',
          _progressColor: 'bg-red-200 dark:bg-red-700',
          _progressBarColor: 'bg-red-600 dark:bg-red-400',
          progressDetails: contentSnapshot.progressDetails?.map(value => {
            return {
              ...value,
              _headlineColor: this.getProgressDetailHeadlineColor(value.headlineColor),
            };
          })
        };
        break;
      }
      case 'ORANGE': {
        result = {
          ...contentSnapshot,
          _cardColor: 'bg-orange-50 dark:bg-orange-900',
          _topHeadlineColor: 'text-orange-600 dark:text-orange-300',
          _headlineColor: 'text-orange-600 dark:text-orange-300',
          _subHeadlineColor: 'text-orange-500 dark:text-orange-300',
          _progressColor: 'bg-orange-200 dark:bg-orange-700',
          _progressBarColor: 'bg-orange-600 dark:bg-orange-400',
          progressDetails: contentSnapshot.progressDetails?.map(value => {
            return {
              ...value,
              _headlineColor: this.getProgressDetailHeadlineColor(value.headlineColor),
            };
          })
        };
        break;
      }
      case 'AMBER': {
        result = {
          ...contentSnapshot,
          _cardColor: 'bg-amber-50 dark:bg-amber-900',
          _topHeadlineColor: 'text-amber-600 dark:text-amber-300',
          _headlineColor: 'text-amber-600 dark:text-amber-300',
          _subHeadlineColor: 'text-amber-500 dark:text-amber-300',
          _progressColor: 'bg-amber-200 dark:bg-amber-700',
          _progressBarColor: 'bg-amber-600 dark:bg-amber-400',
          progressDetails: contentSnapshot.progressDetails?.map(value => {
            return {
              ...value,
              _headlineColor: this.getProgressDetailHeadlineColor(value.headlineColor),
            };
          })
        };
        break;
      }
      case 'YELLOW': {
        result = {
          ...contentSnapshot,
          _cardColor: 'bg-yellow-50 dark:bg-yellow-900',
          _topHeadlineColor: 'text-yellow-600 dark:text-yellow-300',
          _headlineColor: 'text-yellow-600 dark:text-yellow-300',
          _subHeadlineColor: 'text-yellow-500 dark:text-yellow-300',
          _progressColor: 'bg-yellow-200 dark:bg-yellow-700',
          _progressBarColor: 'bg-yellow-600 dark:bg-yellow-400',
          progressDetails: contentSnapshot.progressDetails?.map(value => {
            return {
              ...value,
              _headlineColor: this.getProgressDetailHeadlineColor(value.headlineColor),
            };
          })
        };
        break;
      }
      case 'LIME': {
        result = {
          ...contentSnapshot,
          _cardColor: 'bg-lime-50 dark:bg-lime-900',
          _topHeadlineColor: 'text-lime-600 dark:text-lime-300',
          _headlineColor: 'text-lime-600 dark:text-lime-300',
          _subHeadlineColor: 'text-lime-500 dark:text-lime-300',
          _progressColor: 'bg-lime-200 dark:bg-lime-700',
          _progressBarColor: 'bg-lime-600 dark:bg-lime-400',
          progressDetails: contentSnapshot.progressDetails?.map(value => {
            return {
              ...value,
              _headlineColor: this.getProgressDetailHeadlineColor(value.headlineColor),
            };
          })
        };
        break;
      }
      case 'GREEN': {
        result = {
          ...contentSnapshot,
          _cardColor: 'bg-green-50 dark:bg-green-900',
          _topHeadlineColor: 'text-green-600 dark:text-green-300',
          _headlineColor: 'text-green-600 dark:text-green-300',
          _subHeadlineColor: 'text-green-500 dark:text-green-300',
          _progressColor: 'bg-green-200 dark:bg-green-700',
          _progressBarColor: 'bg-green-600 dark:bg-green-400',
          progressDetails: contentSnapshot.progressDetails?.map(value => {
            return {
              ...value,
              _headlineColor: this.getProgressDetailHeadlineColor(value.headlineColor),
            };
          })
        };
        break;
      }
      case 'EMERALD': {
        result = {
          ...contentSnapshot,
          _cardColor: 'bg-emerald-50 dark:bg-emerald-900',
          _topHeadlineColor: 'text-emerald-600 dark:text-emerald-300',
          _headlineColor: 'text-emerald-600 dark:text-emerald-300',
          _subHeadlineColor: 'text-emerald-500 dark:text-emerald-300',
          _progressColor: 'bg-emerald-200 dark:bg-emerald-700',
          _progressBarColor: 'bg-emerald-600 dark:bg-emerald-400',
          progressDetails: contentSnapshot.progressDetails?.map(value => {
            return {
              ...value,
              _headlineColor: this.getProgressDetailHeadlineColor(value.headlineColor),
            };
          })
        };
        break;
      }
      case 'TEAL': {
        result = {
          ...contentSnapshot,
          _cardColor: 'bg-teal-50 dark:bg-teal-900',
          _topHeadlineColor: 'text-teal-600 dark:text-teal-300',
          _headlineColor: 'text-teal-600 dark:text-teal-300',
          _subHeadlineColor: 'text-teal-500 dark:text-teal-300',
          _progressColor: 'bg-teal-200 dark:bg-teal-700',
          _progressBarColor: 'bg-teal-600 dark:bg-teal-400',
          progressDetails: contentSnapshot.progressDetails?.map(value => {
            return {
              ...value,
              _headlineColor: this.getProgressDetailHeadlineColor(value.headlineColor),
            };
          })
        };
        break;
      }
      case 'CYAN': {
        result = {
          ...contentSnapshot,
          _cardColor: 'bg-cyan-50 dark:bg-cyan-900',
          _topHeadlineColor: 'text-cyan-600 dark:text-cyan-300',
          _headlineColor: 'text-cyan-600 dark:text-cyan-300',
          _subHeadlineColor: 'text-cyan-500 dark:text-cyan-300',
          _progressColor: 'bg-cyan-200 dark:bg-cyan-700',
          _progressBarColor: 'bg-cyan-600 dark:bg-cyan-400',
          progressDetails: contentSnapshot.progressDetails?.map(value => {
            return {
              ...value,
              _headlineColor: this.getProgressDetailHeadlineColor(value.headlineColor),
            };
          })
        };
        break;
      }
      case 'SKY': {
        result = {
          ...contentSnapshot,
          _cardColor: 'bg-sky-50 dark:bg-sky-900',
          _topHeadlineColor: 'text-sky-600 dark:text-sky-300',
          _headlineColor: 'text-sky-600 dark:text-sky-300',
          _subHeadlineColor: 'text-sky-500 dark:text-sky-300',
          _progressColor: 'bg-sky-200 dark:bg-sky-700',
          _progressBarColor: 'bg-sky-600 dark:bg-sky-400',
          progressDetails: contentSnapshot.progressDetails?.map(value => {
            return {
              ...value,
              _headlineColor: this.getProgressDetailHeadlineColor(value.headlineColor),
            };
          })
        };
        break;
      }
      case 'BLUE': {
        result = {
          ...contentSnapshot,
          _cardColor: 'bg-blue-50 dark:bg-blue-900',
          _topHeadlineColor: 'text-blue-600 dark:text-blue-300',
          _headlineColor: 'text-blue-600 dark:text-blue-300',
          _subHeadlineColor: 'text-blue-500 dark:text-blue-300',
          _progressColor: 'bg-blue-200 dark:bg-blue-700',
          _progressBarColor: 'bg-blue-600 dark:bg-blue-400',
          progressDetails: contentSnapshot.progressDetails?.map(value => {
            return {
              ...value,
              _headlineColor: this.getProgressDetailHeadlineColor(value.headlineColor),
            };
          })
        };
        break;
      }
      case 'INDIGO': {
        result = {
          ...contentSnapshot,
          _cardColor: 'bg-indigo-50 dark:bg-indigo-900',
          _topHeadlineColor: 'text-indigo-600 dark:text-indigo-300',
          _headlineColor: 'text-indigo-600 dark:text-indigo-300',
          _subHeadlineColor: 'text-indigo-500 dark:text-indigo-300',
          _progressColor: 'bg-indigo-200 dark:bg-indigo-700',
          _progressBarColor: 'bg-indigo-600 dark:bg-indigo-400',
          progressDetails: contentSnapshot.progressDetails?.map(value => {
            return {
              ...value,
              _headlineColor: this.getProgressDetailHeadlineColor(value.headlineColor),
            };
          })
        };
        break;
      }
      case 'VIOLET': {
        result = {
          ...contentSnapshot,
          _cardColor: 'bg-violet-50 dark:bg-violet-900',
          _topHeadlineColor: 'text-violet-600 dark:text-violet-300',
          _headlineColor: 'text-violet-600 dark:text-violet-300',
          _subHeadlineColor: 'text-violet-500 dark:text-violet-300',
          _progressColor: 'bg-violet-200 dark:bg-violet-700',
          _progressBarColor: 'bg-violet-600 dark:bg-violet-400',
          progressDetails: contentSnapshot.progressDetails?.map(value => {
            return {
              ...value,
              _headlineColor: this.getProgressDetailHeadlineColor(value.headlineColor),
            };
          })
        };
        break;
      }
      case 'PURPLE': {
        result = {
          ...contentSnapshot,
          _cardColor: 'bg-purple-50 dark:bg-purple-900',
          _topHeadlineColor: 'text-purple-600 dark:text-purple-300',
          _headlineColor: 'text-purple-600 dark:text-purple-300',
          _subHeadlineColor: 'text-purple-500 dark:text-purple-300',
          _progressColor: 'bg-purple-200 dark:bg-purple-700',
          _progressBarColor: 'bg-purple-600 dark:bg-purple-400',
          progressDetails: contentSnapshot.progressDetails?.map(value => {
            return {
              ...value,
              _headlineColor: this.getProgressDetailHeadlineColor(value.headlineColor),
            };
          })
        };
        break;
      }
      case 'FUCHSIA': {
        result = {
          ...contentSnapshot,
          _cardColor: 'bg-fuchsia-50 dark:bg-fuchsia-900',
          _topHeadlineColor: 'text-fuchsia-600 dark:text-fuchsia-300',
          _headlineColor: 'text-fuchsia-600 dark:text-fuchsia-300',
          _subHeadlineColor: 'text-fuchsia-500 dark:text-fuchsia-300',
          _progressColor: 'bg-fuchsia-200 dark:bg-fuchsia-700',
          _progressBarColor: 'bg-fuchsia-600 dark:bg-fuchsia-400',
          progressDetails: contentSnapshot.progressDetails?.map(value => {
            return {
              ...value,
              _headlineColor: this.getProgressDetailHeadlineColor(value.headlineColor),
            };
          })
        };
        break;
      }
      case 'PINK': {
        result = {
          ...contentSnapshot,
          _cardColor: 'bg-pink-50 dark:bg-pink-900',
          _topHeadlineColor: 'text-pink-600 dark:text-pink-300',
          _headlineColor: 'text-pink-600 dark:text-pink-300',
          _subHeadlineColor: 'text-pink-500 dark:text-pink-300',
          _progressColor: 'bg-pink-200 dark:bg-pink-700',
          _progressBarColor: 'bg-pink-600 dark:bg-pink-400',
          progressDetails: contentSnapshot.progressDetails?.map(value => {
            return {
              ...value,
              _headlineColor: this.getProgressDetailHeadlineColor(value.headlineColor),
            };
          })
        };
        break;
      }
      case 'ROSE': {
        result = {
          ...contentSnapshot,
          _cardColor: 'bg-rose-50 dark:bg-rose-900',
          _topHeadlineColor: 'text-rose-600 dark:text-rose-300',
          _headlineColor: 'text-rose-600 dark:text-rose-300',
          _subHeadlineColor: 'text-rose-500 dark:text-rose-300',
          _progressColor: 'bg-rose-200 dark:bg-rose-700',
          _progressBarColor: 'bg-rose-600 dark:bg-rose-400',
          progressDetails: contentSnapshot.progressDetails?.map(value => {
            return {
              ...value,
              _headlineColor: this.getProgressDetailHeadlineColor(value.headlineColor),
            };
          })
        };
        break;
      }
      case 'SLATE': {
        result = {
          ...contentSnapshot,
          _cardColor: 'bg-slate-50 dark:bg-slate-900',
          _topHeadlineColor: 'text-slate-600 dark:text-slate-300',
          _headlineColor: 'text-slate-600 dark:text-slate-300',
          _subHeadlineColor: 'text-slate-500 dark:text-slate-300',
          _progressColor: 'bg-slate-200 dark:bg-slate-700',
          _progressBarColor: 'bg-slate-600 dark:bg-slate-400',
          progressDetails: contentSnapshot.progressDetails?.map(value => {
            return {
              ...value,
              _headlineColor: this.getProgressDetailHeadlineColor(value.headlineColor),
            };
          })
        };
        break;
      }
      case 'GRAY': {
        result = {
          ...contentSnapshot,
          _cardColor: 'bg-gray-50 dark:bg-gray-900',
          _topHeadlineColor: 'text-gray-600 dark:text-gray-300',
          _headlineColor: 'text-gray-600 dark:text-gray-300',
          _subHeadlineColor: 'text-gray-500 dark:text-gray-300',
          _progressColor: 'bg-gray-200 dark:bg-gray-700',
          _progressBarColor: 'bg-gray-600 dark:bg-gray-400',
          progressDetails: contentSnapshot.progressDetails?.map(value => {
            return {
              ...value,
              _headlineColor: this.getProgressDetailHeadlineColor(value.headlineColor),
            };
          })
        };
        break;
      }
      case 'ZINC': {
        result = {
          ...contentSnapshot,
          _cardColor: 'bg-zinc-50 dark:bg-zinc-900',
          _topHeadlineColor: 'text-zinc-600 dark:text-zinc-300',
          _headlineColor: 'text-zinc-600 dark:text-zinc-300',
          _subHeadlineColor: 'text-zinc-500 dark:text-zinc-300',
          _progressColor: 'bg-zinc-200 dark:bg-zinc-700',
          _progressBarColor: 'bg-zinc-600 dark:bg-zinc-400',
          progressDetails: contentSnapshot.progressDetails?.map(value => {
            return {
              ...value,
              _headlineColor: this.getProgressDetailHeadlineColor(value.headlineColor),
            };
          })
        };
        break;
      }
      case 'NEUTRAL': {
        result = {
          ...contentSnapshot,
          _cardColor: 'bg-neutral-50 dark:bg-neutral-900',
          _topHeadlineColor: 'text-neutral-600 dark:text-neutral-300',
          _headlineColor: 'text-neutral-600 dark:text-neutral-300',
          _subHeadlineColor: 'text-neutral-500 dark:text-neutral-300',
          _progressColor: 'bg-neutral-200 dark:bg-neutral-700',
          _progressBarColor: 'bg-neutral-600 dark:bg-neutral-400',
          progressDetails: contentSnapshot.progressDetails?.map(value => {
            return {
              ...value,
              _headlineColor: this.getProgressDetailHeadlineColor(value.headlineColor),
            };
          })
        };
        break;
      }
      case 'STONE': {
        result = {
          ...contentSnapshot,
          _cardColor: 'bg-stone-50 dark:bg-stone-900',
          _topHeadlineColor: 'text-stone-600 dark:text-stone-300',
          _headlineColor: 'text-stone-600 dark:text-stone-300',
          _subHeadlineColor: 'text-stone-500 dark:text-stone-300',
          _progressColor: 'bg-stone-200 dark:bg-stone-700',
          _progressBarColor: 'bg-stone-600 dark:bg-stone-400',
          progressDetails: contentSnapshot.progressDetails?.map(value => {
            return {
              ...value,
              _headlineColor: this.getProgressDetailHeadlineColor(value.headlineColor),
            };
          })
        }
      }

    }
    return result;


  });

  private getProgressDetailHeadlineColor(headlineColor: KokuDto.KokuColorEnum | undefined) {
    switch (headlineColor) {
      case 'PRIMARY': {
        return 'text-primary-500 dark:text-primary-300'
      }
      case 'SECONDARY': {
        return 'text-secondary-500 dark:text-secondary-300'
      }
      case 'ACCENT': {
        return 'text-accent-500 dark:text-accent-300'
      }
      case 'INFO': {
        return 'text-info-500 dark:text-info-300'
      }
      case 'SUCCESS': {
        return 'text-success-500 dark:text-success-300'
      }
      case 'WARNING': {
        return 'text-warning-500 dark:text-warning-300'
      }
      case 'ERROR': {
        return 'text-error-500 dark:text-error-300'
      }
      case 'RED': {
        return 'text-red-500 dark:text-red-300'
      }
      case 'ORANGE': {
        return 'text-orange-500 dark:text-orange-300'
      }
      case 'AMBER': {
        return 'text-amber-500 dark:text-amber-300'
      }
      case 'YELLOW': {
        return 'text-yellow-500 dark:text-yellow-300'
      }
      case 'LIME': {
        return 'text-lime-500 dark:text-lime-300'
      }
      case 'GREEN': {
        return 'text-green-500 dark:text-green-300'
      }
      case 'EMERALD': {
        return 'text-emerald-500 dark:text-emerald-300'
      }
      case 'TEAL': {
        return 'text-teal-500 dark:text-teal-300'
      }
      case 'CYAN': {
        return 'text-cyan-500 dark:text-cyan-300'
      }
      case 'SKY': {
        return 'text-sky-500 dark:text-sky-300'
      }
      case 'BLUE': {
        return 'text-blue-500 dark:text-blue-300'
      }
      case 'INDIGO': {
        return 'text-indigo-500 dark:text-indigo-300'
      }
      case 'VIOLET': {
        return 'text-violet-500 dark:text-violet-300'
      }
      case 'PURPLE': {
        return 'text-purple-500 dark:text-purple-300'
      }
      case 'FUCHSIA': {
        return 'text-fuchsia-500 dark:text-fuchsia-300'
      }
      case 'PINK': {
        return 'text-pink-500 dark:text-pink-300'
      }
      case 'ROSE': {
        return 'text-rose-500 dark:text-rose-300'
      }
      case 'SLATE': {
        return 'text-slate-500 dark:text-slate-300'
      }
      case 'GRAY': {
        return 'text-gray-500 dark:text-gray-300'
      }
      case 'ZINC': {
        return 'text-zinc-500 dark:text-zinc-300'
      }
      case 'NEUTRAL': {
        return 'text-neutral-500 dark:text-neutral-300'
      }
      case 'STONE': {
        return 'text-stone-500 dark:text-stone-300'
      }
    }
    return '';
  }

}

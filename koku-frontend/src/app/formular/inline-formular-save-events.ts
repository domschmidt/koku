import { GLOBAL_EVENT_BUS } from '../events/global-events';
import { get } from '../utils/get';

interface InlineFormularSaveEvent {
  '@type'?: string;
  eventName?: string;
  route?: string;
  params?: InlineFormularSaveEventParam[];
}

interface InlineFormularSaveEventParam {
  '@type'?: string;
  param?: string;
  valuePath?: string;
}

export interface InlineFormularSaveEventHandlerContext {
  openRoutedContent(routes: string[]): void;
}

export const executeInlineFormularSaveEvents = (
  events: InlineFormularSaveEvent[] | undefined,
  payload: any,
  context: InlineFormularSaveEventHandlerContext,
) => {
  for (const currentSaveEventJob of events || []) {
    switch (currentSaveEventJob['@type']) {
      case 'propagate-global-event': {
        if (!currentSaveEventJob.eventName) {
          throw new Error(`Missing eventName in saveEvent`);
        }
        GLOBAL_EVENT_BUS.propagateGlobalEvent(currentSaveEventJob.eventName, payload);
        break;
      }
      case 'open-routed-inline-formular': {
        context.openRoutedContent(replaceRouteParams(currentSaveEventJob, payload));
        break;
      }
      default: {
        throw new Error(`Unknown saved event type ${currentSaveEventJob['@type']}`);
      }
    }
  }
};

const replaceRouteParams = (event: InlineFormularSaveEvent, payload: any) => {
  const paramReplacementMapping: Record<string, string> = {};
  for (const currentParamReplacementInfo of event.params || []) {
    switch (currentParamReplacementInfo['@type']) {
      case 'event-payload': {
        if (currentParamReplacementInfo.param !== undefined && currentParamReplacementInfo.valuePath !== undefined) {
          const valueRawOrNull = get(payload, currentParamReplacementInfo.valuePath, null);
          if (valueRawOrNull !== null) {
            paramReplacementMapping[currentParamReplacementInfo.param] = String(valueRawOrNull);
          }
        }
        break;
      }
    }
  }

  const replacedRouteParts: string[] = [];
  for (const currentRouteRaw of event.route?.split('/') || []) {
    replacedRouteParts.push(paramReplacementMapping[currentRouteRaw] ?? currentRouteRaw);
  }
  return replacedRouteParts;
};

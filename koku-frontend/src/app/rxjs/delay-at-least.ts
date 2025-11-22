import {combineLatest, map, Observable, OperatorFunction, timer} from 'rxjs';

export function delayAtLeast<T>(delay: number): OperatorFunction<T, T> {
  return function(source$: Observable<T>): Observable<T> {
    if (delay === 0) return source$;
    return combineLatest([timer(delay), source$]).pipe(map(x => x[1]));
  }
}

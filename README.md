# Android AppTour

AppTourライブラリをForkしてリファクタ・独自機能を追加したライブラリ。

このライブラリの基本的な使い方については [vlonjatg/AppTour](https://github.com/vlonjatg/AppTour) を参照すること。

## AppTourとの違い

 * AppTour(Activity)を継承しなくても問題ない設計となっている
  * 互換性のため、AppTour自体は残されている
 * AppTourDelegate.AppTourCompatCompatインターフェースを継承し、必要なメソッドをimplする
 * AppTourDelegateのonCreate等を必要なActivityやFragmentのタイミングでコールすることで、同等の動作を実現している

## 追加されている機能

 * KitKatでのImmersiveモード対応
 * Lollipopでのステータスバー・ナビゲーションバーカラー対応

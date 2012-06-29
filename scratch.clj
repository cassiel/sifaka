(require '(clojure.contrib [prxml :as p]))

(with-out-str (p/prxml [:p]))

(with-out-str (p/prxml [:p {:class "greet"} [:i "Ladies & gentlemen"]]))

;; Misc. for Max experiments:

(eval (read-string "(+ 2 2)"))

((eval (read-string "inc")) 45)

((eval (read-string "#(+ % 10)")) 45)

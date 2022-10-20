(ns yhnam.pattern-match)

(defn process-vars
  [vars]
  (letfn [(process-var [var]
            (if-not (symbol? var)
              (gensym "ocr-")
              var))]
    (vec (map process-var vars))))

(defn make-default-match [vars cs]
  (let [cs (partition 2 cs)
        [p a] (last cs) ;; 심볼의 경우 p를 a에 바인딩하는 기능 추가 필요.
        last-match (vec (repeat (count vars) '_))]
    (if (= p :else)
      (conj (vec (butlast cs)) [last-match a])
      (throw (RuntimeException. "last match must be :else ")))))

(defn make-pattern-let-binding
  "let 바인딩을 위한 자료구조 생성"
  [vs vars]
  (interleave vs vars))

(defn make-cond
  "cond predicate을 만들기 위한 비교문"
  [vs cls]
  (map (fn [v c]
         `(= ~v ~c)) vs cls))

(def backtrack-exception (Exception. "BackTrack!"))

(defn catch-error
  "예외를 잡는 자료구조 추가"
  [& body]
  `(catch Exception e#
     (if (identical? e# ~'backtrack-exception)
       (do
         ~@body)
       (throw e#))))

(defn compile-rec
  "재귀적으로 try문 안에 있는 비교문을 생성."
  [cnds return]
  (let [cnd (first cnds)
        [v c] (vec (rest cnd))] ;; c가 심볼인 경우 v를 바인딩하도록 해야함.
    (if (seq cnd)
      (cond
        (symbol? c) `(let [~c ~v] (do ~(compile-rec (rest cnds) return)))
        (= '_ c) `(do ~(compile-rec (rest cnds) return))
        :else `(do (cond ~cnd ~(compile-rec (rest cnds) return)
                         :else ~'(throw backtrack-exception))))
      return)))

(defn match-compile
  [conds+return]
  (let [[cnds return] (first conds+return)
        cnd (first cnds)
        [v c] (vec (rest cnd))] ;; c가 심볼인 경우 v를 바인딩하도록 해야함.
    (if (seq cnd)
      (cond
        (symbol? c) `(let [~c ~v] (try ~(compile-rec (rest cnds) return)
                                     ~(catch-error (match-compile (rest conds+return)))))
        (= '_ c) `(try ~(compile-rec (rest cnds) return)
                       ~(catch-error (match-compile (rest conds+return))))
        :else `(try (cond ~cnd ~(compile-rec (rest cnds) return)
                          :else ~'(throw backtrack-exception))
                    ~(catch-error (match-compile (rest conds+return)))))
      return)))

;; TODO : 값이 LIST가 아니면 LIST로 감싸기.
(defmacro yhnam-match
  "간단한(버그투성이) 패턴매칭 프로토타입."
  [vars & clauses]
  (let [vs (process-vars vars)
        cs (make-default-match vars clauses)
        pattern-let-binding (vec (make-pattern-let-binding vs vars))
        conds (map (fn [c] [(make-cond vs (first c)) (second c)]) cs)]
    `(let ~pattern-let-binding
       ~(match-compile conds))))



(defmacro match [m-pred & body]
  (let [res (loop [pre nil
                   cur (partition-all 2 body)
                   res []]
              (if (seq cur)
                (cond
                  (= (-> cur first second) :where)
                  (recur (first cur) (rest cur) res)

                  (= (second pre) :where)
                  (recur nil (rest cur) (conj res
                                              `(~(first pre)
                                                (if ~(ffirst cur)
                                                  ~(-> cur first second)
                                                  (throw backtrack-exception)))))
                  :else (recur nil (rest cur) (conj res (first cur))))
                res))]
    `(yhnam-match ~m-pred ~@(apply concat res))))

(comment
  ;; should return MOOYAHO
  (match [1 2]
      [a b] :where (do (println a b "왓숑...") false)
      "HI"

      [c d] :where (do (println c d "여디도 왔엉") false)
      "GOOD"

      [e f] :where (do (println e f "여기 마지막") true)
      "MOOYAHO"

      :else
      "NEVER")

  (doseq [n (range 1 101)]
    (println
     (match [(mod n 3) (mod n 5)]
               [0 0] (str "FizzBuzz")
               [0 _] (str "Fizz")
               [_ 0] (str "Buzz")
               :else n)))

  (match [1 2]
      [a b] (if (= a 1) (throw backtrack-exception))
      [c e] "HERE"
      :else "ELSE")
  ;;
  )

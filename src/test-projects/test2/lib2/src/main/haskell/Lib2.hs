module Lib2 where

import Lib1
import Text.PrettyPrint.ANSI.Leijen

printGreeting :: String -> IO ()
printGreeting greeting = putDoc $ text (hello greeting) <> linebreak

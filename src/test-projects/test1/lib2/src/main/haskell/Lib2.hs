module Lib2 where

import Text.PrettyPrint.ANSI.Leijen

printGreeting :: String -> IO ()
printGreeting greeting = putDoc $ text greeting <> linebreak

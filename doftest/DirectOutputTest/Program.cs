using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;
using System.Threading;
using System.Threading.Tasks;
using DirectOutput;
using DirectOutput.Cab;
using DirectOutput.Cab.Out;
using DirectOutput.Cab.Out.LW;
using DirectOutput.Cab.Out.PS;
using DirectOutput.Cab.Toys;
using DirectOutput.Cab.Toys.LWEquivalent;
using System.Runtime.InteropServices;
using System.Text.RegularExpressions;


namespace DirectOutputTest
{
    class Program
    {
        static void Main(string[] args)
        {
            // set up the path so that we can find LedWiz.dll and other third-party DLLs
            String path = Environment.GetEnvironmentVariable("PATH");
            Environment.SetEnvironmentVariable("PATH", path + ";d:\\Program Files (x86)\\Visual Pinball\\DirectOutput");

            // start reading the arguments
            int argi = 0;
            int argc = args.Length;
			int ival;

			// auto-configure the cab
			Cabinet cab = new Cabinet();
			cab.Init(new CabinetOwner());
			cab.AutoConfig();

			// if there's no unit index, show the list of units
			if (argi >= argc)
			{
				// show LedWiz units
				List<int> ledWizList = LedWiz.GetLedwizNumbers();
				if (ledWizList.Count > 0)
				{
					Console.Out.WriteLine("LedWiz units detected:");
					foreach (int i in ledWizList)
					{
						LedWiz lw = new LedWiz(i);
                        Console.Out.WriteLine("  LedWiz unit " + lw.Number + ": " + lw.Name + " " + lw.Outputs.Count + " outputs");
                    }
				}
				else
					Console.Out.WriteLine("LedWiz units detected: none");

				// show Pinscape units
				IEnumerable<int> pinscapeList = Pinscape.AllDevices().Select(d => d.UnitNo());
				if (pinscapeList.Count() > 0)
				{
					Console.Out.WriteLine("Pinscape units detected:");
					foreach (int i in pinscapeList)
					{
						Pinscape ps = new Pinscape(i);
						Console.Out.WriteLine("  Pinscape unit " + ps.Number + ": " + ps.Name + " " + ps.NumberOfOutputs + " outputs");
					}
				}
				else
					Console.Out.WriteLine("Pinscape units detected: none");

				// show LedWizEquivalent units
				Console.Out.WriteLine("\nLedWiz Equivalent Units:");
				foreach (LedWizEquivalent lwe in cab.Toys.Where(t => t is LedWizEquivalent))
					Console.Out.WriteLine("  " + lwe.Name + ", unit " + lwe.LedWizNumber);
			}
			else
			{
				// parse the unit index
				int unitNo = 1;
				if (argi < argc && int.TryParse(args[argi++], out ival))
					unitNo = ival;

				// get the LWE for the selected unit number
				LedWizEquivalent lwe = (LedWizEquivalent)cab.Toys.FirstOrDefault(
					T => T is LedWizEquivalent && ((LedWizEquivalent)T).LedWizNumber == unitNo);
				if (lwe != null)
				{
					// get the output controller matching this item
					IOutputController oc = cab.OutputControllers.FirstOrDefault(c => c.Name + " Equivalent" == lwe.Name);

					// initialize it
					oc.Init(cab);

                    // process the output list from the command line
                    processOuts(oc, args, argi, argc);
					
					// wait for input to let the user observe the output settings
					Console.Out.WriteLine("Ready: set outputs with <Out#> <Val> ...");
                    Console.Out.WriteLine("Set multiple outputs at once with <Out#>-<Out#> <Val> or <Out#>,<Out#>,... <Val>");
                    Console.Out.WriteLine("Set all outputs at once with * <val>");
                    Console.Out.WriteLine("Pause with SLEEP <time>; this can be used before or after an output setting.");
                    Console.Out.WriteLine("Type QUIT to exit.");
                    for (;;)
                    {
                        Console.Out.Write(">");
                        String l = Console.ReadLine().Trim();
                        if (l == "")
                        {
                            // ignore blank lines
                        }
                        else if (Regex.Match(l, "(?i)q(uit)?").Success)
                        {
                            break;
                        }
                        else
                        {
                            String[] la = l.Split(' ');
                            processOuts(oc, la, 0, la.Length);
                        }
                    }
					
					// done with the DOF objects
					oc.Finish();
					lwe.Finish();
				}
				else
				{
					Console.Out.WriteLine("No such unit");
				}
			}
        }

        protected static void processOuts(IOutputController oc, String[] args, int argi, int argc)
        {
            // get the output list
            OutputList outs = oc.Outputs;

            // set each output as requested
            for (; argi < argc; argi += 2)
            {
                // The special syntax "." simply pauses briefly (100ms)
                if (args[argi] == ".")
                {
                    oc.Update();
                    Thread.Sleep(100);
                    argi -= 1;
                    continue;
                }

                // SLEEP <seconds> pauses for the given time, which can be
                // expressed with a floating point value for fractional seconds
                double d;
                if (args[argi].ToLower() == "sleep" && argi+1 < argc && Double.TryParse(args[argi+1], out d))
                {
                    oc.Update();
                    Thread.Sleep(TimeSpan.FromSeconds(d));
                    continue;
                }

                // Anything else is an output setting.  The next two arguments
                // give the output number(s) and the brightness value to set.
                // The output list can be comma-delimited, so start by splitting
                // the list at commas, and process each element.
                foreach (String ai in args[argi].Split(','))
                {
                    // Each comma-delimited element can look like:
                    //
                    //    Start-End    - set all outputs in the range (e.g., 16-32)
                    //    Start-       - set all outputs from Start to last (e.g., 16-)
                    //    -End         - set all outputs from 1 to End (e.g., -16)
                    //    *            - set all ports
                    //    Port         - set the single port (e.g., 16)
                    Match m;
                    int outIdxFirst, outIdxLast, outVal;
                    if ((m = Regex.Match(ai, @"(\d+)-(\d+)")).Success)
                    {
                        outIdxFirst = int.Parse(m.Groups[1].Value);
                        outIdxLast = int.Parse(m.Groups[2].Value);
                    }
                    else if ((m = Regex.Match(ai, @"(\d+)-")).Success)
                    {
                        outIdxFirst = int.Parse(m.Groups[1].Value);
                        outIdxLast = outs.Count;
                    }
                    else if ((m = Regex.Match(ai, @"-(\d+)")).Success)
                    {
                        outIdxFirst = 1;
                        outIdxLast = outs.Count;
                    }
                    else if (ai == "*")
                    {
                        outIdxFirst = 1;
                        outIdxLast = outs.Count;
                    }
                    else if (int.TryParse(ai, out outIdxFirst))
                    {
                        outIdxLast = outIdxFirst;
                    }
                    else
                    {
                        // invalid syntax - complain and go to the next output in the list
                        Console.WriteLine("Expected an output number, but found '" + ai + "'");
                        break;
                    }

                    // We have the port.  Set up a description of the selection and check that
                    // the start and end values are in range.
                    String outIdx = outIdxFirst + (outIdxLast == outIdxFirst ? "" : "-" + outIdxLast);
                    if (outIdxFirst < 1)
                        Console.WriteLine("Output ID " + outIdxFirst + " is out of range (must be 1-" + outs.Count + ")");
                    else if (outIdxLast > outs.Count)
                        Console.WriteLine("Output ID " + outIdxLast + " is out of range (must be 1-" + outs.Count + ")");
                    else
                    {
                        // The port range is valid.  Read the output value and validate
                        // it.  If it's invalid, break out of the comma loop, since all
                        // outputs in the list will fail with the same error.
                        if (argi + 1 >= argc)
                        {
                            Console.WriteLine("Missing output value for output " + outIdx);
                            break;
                        }
                        else if (!int.TryParse(args[argi + 1], out outVal))
                        {
                            Console.WriteLine("Invalid output value '" + args[argi + 1] + "'");
                            break;
                        }
                        else if (outVal < 0 || outVal > 255)
                        {
                            Console.WriteLine("Output value " + outVal + " out of range (must be 1-255)");
                            break;
                        }
                        else
                        {
                            // Valid output setting.  Set all ports in the range.
                            Console.WriteLine("Setting output #" + outIdx + " to " + outVal);
                            for (int j = outIdxFirst; j <= outIdxLast; ++j)
                                outs[j - 1].Value = (byte)outVal;
                        }
                    }
                }
            }

            // flush updates
            oc.Update();
        }

    } 
}
